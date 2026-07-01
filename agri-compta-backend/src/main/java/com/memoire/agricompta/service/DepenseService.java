package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Depense;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.DepenseRepository;
import com.memoire.agricompta.web.dto.DepenseRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepenseService {
    private final DepenseRepository repository;
    private final LookupService lookupService;

    public List<Depense> findAll() {
        return repository.findAll();
    }

    public Depense findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depense introuvable : " + id));
    }

    public Depense create(DepenseRequest request) {
        Depense depense = new Depense();
        apply(depense, request);
        return repository.save(depense);
    }

    public Depense update(Long id, DepenseRequest request) {
        Depense depense = findById(id);
        apply(depense, request);
        return repository.save(depense);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
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
        depense.setUtilisateur(request.utilisateurId() == null ? null : lookupService.utilisateur(request.utilisateurId()));
    }
}
