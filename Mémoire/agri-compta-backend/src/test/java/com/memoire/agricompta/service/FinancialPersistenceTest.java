package com.memoire.agricompta.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.memoire.agricompta.domain.entity.*;
import com.memoire.agricompta.domain.enums.*;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.*;
import com.memoire.agricompta.web.dto.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.datasource.url=jdbc:h2:mem:roles;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop" })
@Transactional
class FinancialPersistenceTest {
    @Autowired EntityManager em;
    @Autowired DepenseService depenses;
    @Autowired RecetteService recettes;
    @Autowired DashboardService dashboard;
    @Autowired RapportService rapports;
    @Autowired DepenseRepository depenseRepository;
    @Autowired RecetteRepository recetteRepository;
    @MockitoBean AuthContext auth;
    private Exploitation farm;
    private Culture culture;
    private CategorieDepense categorie;
    private Utilisateur user;
    private final LocalDate day = LocalDate.of(2026, 1, 15);

    @BeforeEach
    void setup() {
        farm = new Exploitation(); farm.setNom("Test " + UUID.randomUUID()); em.persist(farm);
        CampagneAgricole campagne = new CampagneAgricole(); campagne.setNom("Campagne");
        campagne.setDateDebut(day); campagne.setExploitation(farm); em.persist(campagne);
        Parcelle parcelle = new Parcelle(); parcelle.setNom("Parcelle"); parcelle.setSuperficieHa(BigDecimal.ONE);
        parcelle.setExploitation(farm); em.persist(parcelle);
        culture = new Culture(); culture.setNom("Tomate"); culture.setSurfaceHa(BigDecimal.ONE);
        culture.setStatut(StatutCulture.EN_COURS); culture.setCampagne(campagne); culture.setParcelle(parcelle);
        culture.setExploitation(farm); em.persist(culture);
        categorie = new CategorieDepense(); categorie.setNom("Test " + UUID.randomUUID()); em.persist(categorie);
        user = new Utilisateur(); user.setNom("Test"); user.setPrenom("Test");
        user.setEmail(UUID.randomUUID() + "@example.test"); user.setMotDePasse("test-hash");
        user.setRole(Role.COMPTABLE); user.setExploitation(farm); em.persist(user);
        em.flush();
        when(auth.currentExploitationId()).thenReturn(farm.getId());
        when(auth.currentUser()).thenReturn(new AuthTokenService.AuthenticatedUser(user.getId(), Role.COMPTABLE, user.getEmail(), farm.getId()));
        when(auth.owns(any())).thenAnswer(invocation -> {
            Exploitation value = invocation.getArgument(0);
            return value != null && value.getId().equals(farm.getId());
        });
    }

    private DepenseRequest charge(String montant) {
        return new DepenseRequest("Semences", new BigDecimal(montant), day, "Espèces", "REF-1",
                culture.getId(), categorie.getId(), null, 999999L);
    }

    private RecetteRequest vente(String montant) {
        return new RecetteRequest("Vente", new BigDecimal(montant), day, BigDecimal.ONE, "KG",
                new BigDecimal(montant), culture.getId(), 999999L);
    }

    @Test
    void correctionAndCancellationAreAuditedAndCancelledAmountsLeaveEveryTotal() {
        Depense depense = depenses.create(charge("100"));
        Recette recette = recettes.create(vente("500"));
        assertEquals(user.getId(), depense.getUtilisateur().getId(), "Client-supplied author must be ignored");
        assertEquals(user.getId(), recette.getUtilisateur().getId());
        depenses.update(depense.getId(), charge("150"));
        recettes.update(recette.getId(), vente("600"));
        assertEquals(0, dashboard.getDashboard().beneficeGlobal().compareTo(new BigDecimal("450")));
        var history = depenses.history(depense.getId());
        assertEquals(2, history.size());
        assertEquals("MODIFICATION", history.getFirst().getAction());
        assertTrue(history.getFirst().getAvant().contains("100"));
        assertTrue(history.getFirst().getApres().contains("150"));
        assertEquals(user.getEmail(), history.getFirst().getAuteur());

        depenses.cancel(depense.getId(), "Double saisie");
        recettes.cancel(recette.getId(), "Vente annulée");
        em.flush(); em.clear();
        assertTrue(depenseRepository.findById(depense.getId()).orElseThrow().isAnnule());
        assertTrue(recetteRepository.findById(recette.getId()).orElseThrow().isAnnule());
        assertEquals(3, depenses.history(depense.getId()).size());
        assertEquals("ANNULATION", depenses.history(depense.getId()).getFirst().getAction());
        assertEquals("Double saisie", depenses.history(depense.getId()).getFirst().getMotif());
        assertEquals(0, dashboard.getDashboard().totalDepenses().compareTo(BigDecimal.ZERO));
        assertEquals(0, dashboard.getDashboard().totalRecettes().compareTo(BigDecimal.ZERO));
        var rapport = rapports.synthese(day, day, null, null);
        assertEquals(0, rapport.totalDepenses().compareTo(BigDecimal.ZERO));
        assertEquals(0, rapport.totalRecettes().compareTo(BigDecimal.ZERO));
        assertEquals(0, depenseRepository.totalByCultureId(culture.getId()).compareTo(BigDecimal.ZERO));
        assertEquals(0, recetteRepository.totalByCultureId(culture.getId()).compareTo(BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> depenses.update(depense.getId(), charge("1")));
        assertThrows(BusinessException.class, () -> recettes.cancel(recette.getId(), "Deuxième annulation"));
    }

    @Test
    void foreignFinancialRecordsCannotBeReadCorrectedCancelledOrAudited() {
        Depense depense = depenses.create(charge("100"));
        Recette recette = recettes.create(vente("500"));
        when(auth.owns(any())).thenReturn(false);
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class, () -> depenses.findById(depense.getId())),
                () -> assertThrows(ResourceNotFoundException.class, () -> recettes.findById(recette.getId())),
                () -> assertThrows(ResourceNotFoundException.class, () -> depenses.update(depense.getId(), charge("1"))),
                () -> assertThrows(ResourceNotFoundException.class, () -> recettes.update(recette.getId(), vente("1"))),
                () -> assertThrows(ResourceNotFoundException.class, () -> depenses.cancel(depense.getId(), "Motif")),
                () -> assertThrows(ResourceNotFoundException.class, () -> recettes.cancel(recette.getId(), "Motif")),
                () -> assertThrows(ResourceNotFoundException.class, () -> depenses.history(depense.getId())),
                () -> assertThrows(ResourceNotFoundException.class, () -> recettes.history(recette.getId())));
    }

    @Test
    void cancellationNeedsAMeaningfulReason() {
        Depense depense = depenses.create(charge("100"));
        Recette recette = recettes.create(vente("500"));
        assertThrows(BusinessException.class, () -> depenses.cancel(depense.getId(), "   "));
        assertThrows(BusinessException.class, () -> recettes.cancel(recette.getId(), "x".repeat(501)));
        assertFalse(depense.isAnnule());
        assertFalse(recette.isAnnule());
    }
}
