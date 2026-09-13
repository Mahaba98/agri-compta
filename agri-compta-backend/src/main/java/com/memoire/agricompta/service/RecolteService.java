package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Culture;
import com.memoire.agricompta.domain.entity.Recolte;
import com.memoire.agricompta.domain.enums.StatutCulture;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.RecolteRepository;
import com.memoire.agricompta.web.dto.RecolteRequest;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecolteService {
    private final RecolteRepository repository;
    private final LookupService lookupService;
    private final AuthContext authContext;

    public List<Recolte> findAll() {
        return repository.findByExploitationId(authContext.currentExploitationId());
    }

    public Recolte findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recolte introuvable : " + id));
    }

    public Recolte create(RecolteRequest request) {
        Recolte recolte = new Recolte();
        apply(recolte, request);
        recolte.setExploitation(lookupService.exploitation(authContext.currentExploitationId()));
        return repository.save(recolte);
    }

    public Recolte update(Long id, RecolteRequest request) {
        Recolte recolte = findById(id);
        apply(recolte, request);
        return repository.save(recolte);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(Recolte recolte, RecolteRequest request) {
        Culture culture = lookupService.culture(request.cultureId());
        ensureCultureCanBeHarvested(culture);

        recolte.setDateRecolte(request.dateRecolte());
        recolte.setQuantite(request.quantite());
        recolte.setUnite(request.unite());
        recolte.setPrixUnitaire(request.prixUnitaire());
        recolte.setMontantTotal(resolveMontantTotal(request));
        recolte.setObservation(request.observation());
        recolte.setCulture(culture);
        recolte.setUtilisateur(request.utilisateurId() == null ? null : lookupService.utilisateur(request.utilisateurId()));

        culture.setStatut(StatutCulture.RECOLTEE);
        if (culture.getDateRecolteReelle() == null || request.dateRecolte().isAfter(culture.getDateRecolteReelle())) {
            culture.setDateRecolteReelle(request.dateRecolte());
        }
    }

    private void ensureCultureCanBeHarvested(Culture culture) {
        if (culture.getStatut() == StatutCulture.ANNULEE) {
            throw new BusinessException("Impossible d'enregistrer une recolte pour une culture annulee.");
        }
        if (culture.getStatut() == StatutCulture.PLANIFIEE) {
            throw new BusinessException("La culture doit etre en cours avant d'enregistrer une recolte.");
        }
    }

    private BigDecimal resolveMontantTotal(RecolteRequest request) {
        if (request.montantTotal() != null) {
            return request.montantTotal();
        }
        if (request.prixUnitaire() == null) {
            return null;
        }
        return request.quantite().multiply(request.prixUnitaire());
    }
}
