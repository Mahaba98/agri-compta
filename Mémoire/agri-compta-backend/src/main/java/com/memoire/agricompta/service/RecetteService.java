package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Recette;
import com.memoire.agricompta.domain.entity.JournalFinancier;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.RecetteRepository;
import com.memoire.agricompta.web.dto.RecetteRequest;
import java.util.List;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecetteService {
    private final RecetteRepository repository;
    private final LookupService lookupService;
    private final AuthContext authContext;
    private final JournalFinancierService journal;

    public List<Recette> findAll() {
        return repository.findByExploitationId(authContext.currentExploitationId());
    }

    public Recette findById(Long id) {
        return repository.findById(id)
                .filter(value -> authContext.owns(value.getExploitation()))
                .orElseThrow(() -> new ResourceNotFoundException("Recette introuvable : " + id));
    }

    public Recette create(RecetteRequest request) {
        Recette recette = new Recette();
        apply(recette, request);
        recette.setExploitation(lookupService.exploitation(authContext.currentExploitationId()));
        Recette saved = repository.save(recette);
        journal.record("RECETTE", saved.getId(), "CREATION", null, snapshot(saved), null);
        return saved;
    }

    public Recette update(Long id, RecetteRequest request) {
        Recette recette = findById(id);
        ensureActive(recette);
        RecetteRequest avant = snapshot(recette);
        apply(recette, request);
        Recette saved = repository.save(recette);
        journal.record("RECETTE", id, "MODIFICATION", avant, snapshot(saved), null);
        return saved;
    }

    public Recette cancel(Long id, String motif) {
        Recette recette = findById(id);
        ensureActive(recette);
        if (motif == null || motif.isBlank() || motif.length() > 500) {
            throw new BusinessException("Un motif d'annulation de 1 à 500 caractères est obligatoire.");
        }
        RecetteRequest avant = snapshot(recette);
        recette.setAnnule(true);
        recette.setMotifAnnulation(motif.trim());
        recette.setDateAnnulation(LocalDateTime.now());
        journal.record("RECETTE", id, "ANNULATION", avant, null, motif.trim());
        return repository.save(recette);
    }

    public List<JournalFinancier> history(Long id) {
        findById(id);
        return journal.history("RECETTE", id);
    }

    private void ensureActive(Recette recette) {
        if (recette.isAnnule()) {
            throw new BusinessException("Cette recette est annulée et ne peut plus être modifiée.");
        }
    }

    private RecetteRequest snapshot(Recette recette) {
        return new RecetteRequest(recette.getLibelle(), recette.getMontant(), recette.getDateRecette(),
                recette.getQuantite(), recette.getUnite(), recette.getPrixUnitaire(), recette.getCulture().getId(),
                recette.getUtilisateur() == null ? null : recette.getUtilisateur().getId());
    }

    private void apply(Recette recette, RecetteRequest request) {
        recette.setLibelle(request.libelle());
        recette.setMontant(request.montant());
        recette.setDateRecette(request.dateRecette());
        recette.setQuantite(request.quantite());
        recette.setUnite(request.unite());
        recette.setPrixUnitaire(request.prixUnitaire());
        recette.setCulture(lookupService.culture(request.cultureId()));
        recette.setUtilisateur(lookupService.utilisateur(authContext.currentUser().id()));
    }
}
