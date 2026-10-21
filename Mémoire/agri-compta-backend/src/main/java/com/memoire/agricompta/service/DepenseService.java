package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Depense;
import com.memoire.agricompta.domain.entity.JournalFinancier;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.DepenseRepository;
import com.memoire.agricompta.web.dto.DepenseRequest;
import java.util.List;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepenseService {
    private final DepenseRepository repository;
    private final LookupService lookupService;
    private final AuthContext authContext;
    private final JournalFinancierService journal;

    public List<Depense> findAll() {
        return repository.findByExploitationId(authContext.currentExploitationId());
    }

    public Depense findById(Long id) {
        return repository.findById(id)
                .filter(value -> authContext.owns(value.getExploitation()))
                .orElseThrow(() -> new ResourceNotFoundException("Depense introuvable : " + id));
    }

    public Depense create(DepenseRequest request) {
        Depense depense = new Depense();
        apply(depense, request);
        depense.setExploitation(lookupService.exploitation(authContext.currentExploitationId()));
        Depense saved = repository.save(depense);
        journal.record("DEPENSE", saved.getId(), "CREATION", null, snapshot(saved), null);
        return saved;
    }

    public Depense update(Long id, DepenseRequest request) {
        Depense depense = findById(id);
        ensureActive(depense);
        DepenseRequest avant = snapshot(depense);
        apply(depense, request);
        Depense saved = repository.save(depense);
        journal.record("DEPENSE", id, "MODIFICATION", avant, snapshot(saved), null);
        return saved;
    }

    public Depense cancel(Long id, String motif) {
        Depense depense = findById(id);
        ensureActive(depense);
        if (motif == null || motif.isBlank() || motif.length() > 500) {
            throw new BusinessException("Un motif d'annulation de 1 à 500 caractères est obligatoire.");
        }
        DepenseRequest avant = snapshot(depense);
        depense.setAnnule(true);
        depense.setMotifAnnulation(motif.trim());
        depense.setDateAnnulation(LocalDateTime.now());
        journal.record("DEPENSE", id, "ANNULATION", avant, null, motif.trim());
        return repository.save(depense);
    }

    public List<JournalFinancier> history(Long id) {
        findById(id);
        return journal.history("DEPENSE", id);
    }

    private void ensureActive(Depense depense) {
        if (depense.isAnnule()) {
            throw new BusinessException("Cette dépense est annulée et ne peut plus être modifiée.");
        }
    }

    private DepenseRequest snapshot(Depense depense) {
        return new DepenseRequest(depense.getLibelle(), depense.getMontant(), depense.getDateDepense(),
                depense.getModePaiement(), depense.getReferencePiece(), depense.getCulture().getId(),
                depense.getCategorie().getId(), depense.getOperationAgricole() == null ? null : depense.getOperationAgricole().getId(),
                depense.getUtilisateur() == null ? null : depense.getUtilisateur().getId());
    }

    private void apply(Depense depense, DepenseRequest request) {
        depense.setLibelle(request.libelle());
        depense.setMontant(request.montant());
        depense.setDateDepense(request.dateDepense());
        depense.setModePaiement(request.modePaiement());
        depense.setReferencePiece(request.referencePiece());
        depense.setCulture(lookupService.culture(request.cultureId()));
        depense.setCategorie(lookupService.categorie(request.categorieId()));
        depense.setOperationAgricole(request.operationAgricoleId() == null ? null : lookupService.operation(request.operationAgricoleId()));
        depense.setUtilisateur(lookupService.utilisateur(authContext.currentUser().id()));
    }
}
