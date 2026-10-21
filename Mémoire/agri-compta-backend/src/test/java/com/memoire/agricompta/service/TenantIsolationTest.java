package com.memoire.agricompta.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.memoire.agricompta.config.ApiAuthenticationFilter;
import com.memoire.agricompta.domain.entity.*;
import com.memoire.agricompta.domain.enums.Role;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;

class TenantIsolationTest {
    private final CampagneAgricoleRepository campagnes = mock(CampagneAgricoleRepository.class);
    private final ParcelleRepository parcelles = mock(ParcelleRepository.class);
    private final CultureRepository cultures = mock(CultureRepository.class);
    private final CategorieDepenseRepository categories = mock(CategorieDepenseRepository.class);
    private final OperationAgricoleRepository operations = mock(OperationAgricoleRepository.class);
    private final ProduitStockRepository produits = mock(ProduitStockRepository.class);
    private final UtilisateurRepository users = mock(UtilisateurRepository.class);
    private final ExploitationRepository farms = mock(ExploitationRepository.class);

    private AuthContext context(Role role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(ApiAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE,
                new AuthTokenService.AuthenticatedUser(1L, role, "test@example.test", 10L));
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        factory.addBean("request", request);
        return new AuthContext(factory.getBeanProvider(jakarta.servlet.http.HttpServletRequest.class));
    }

    private LookupService lookup(AuthContext context) {
        return new LookupService(campagnes, parcelles, cultures, categories, operations, produits, users, farms, context);
    }

    @Test
    void foreignIdentifiersAreNotAvailableToServicesOrAssociationLookupsEvenForAdmin() {
        for (Role role : Role.values()) {
            AuthContext auth = context(role); LookupService lookup = lookup(auth);
            Exploitation other = new Exploitation(); other.setId(20L);
            Culture culture = new Culture(); culture.setExploitation(other);
            Parcelle parcelle = new Parcelle(); parcelle.setExploitation(other);
            CampagneAgricole campagne = new CampagneAgricole(); campagne.setExploitation(other);
            OperationAgricole operation = new OperationAgricole(); operation.setExploitation(other);
            ProduitStock produit = new ProduitStock(); produit.setExploitation(other);
            Utilisateur user = new Utilisateur(); user.setExploitation(other);
            when(cultures.findById(99L)).thenReturn(Optional.of(culture));
            when(parcelles.findById(99L)).thenReturn(Optional.of(parcelle));
            when(campagnes.findById(99L)).thenReturn(Optional.of(campagne));
            when(operations.findById(99L)).thenReturn(Optional.of(operation));
            when(produits.findById(99L)).thenReturn(Optional.of(produit));
            when(users.findById(99L)).thenReturn(Optional.of(user));
            assertAll(
                    () -> assertThrows(ResourceNotFoundException.class, () -> lookup.culture(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> lookup.parcelle(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> lookup.campagne(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> lookup.operation(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> lookup.produitStock(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> lookup.utilisateur(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> new CultureService(cultures, lookup, auth).findById(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> new ParcelleService(parcelles, auth, lookup).findById(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> new CampagneAgricoleService(campagnes, auth, lookup).findById(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> new OperationAgricoleService(operations, lookup, auth).findById(99L)),
                    () -> assertThrows(ResourceNotFoundException.class, () -> new ProduitStockService(produits, auth, lookup).findById(99L)));
        }
    }

    @Test
    void nonAdminCanReadOnlyTheirOwnExploitation() {
        AuthContext auth = context(Role.COMPTABLE);
        Exploitation other = new Exploitation(); other.setId(20L);
        when(farms.findById(20L)).thenReturn(Optional.of(other));
        assertThrows(ResourceNotFoundException.class, () -> new ExploitationService(farms, auth).findById(20L));
        assertThrows(ResourceNotFoundException.class, () -> lookup(auth).exploitation(20L));
        other.setId(10L);
        when(farms.findById(10L)).thenReturn(Optional.of(other));
        assertEquals(1, new ExploitationService(farms, auth).findAll().size());
    }
}
