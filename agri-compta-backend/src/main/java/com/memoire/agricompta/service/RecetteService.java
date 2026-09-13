package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Recette;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.RecetteRepository;
import com.memoire.agricompta.web.dto.RecetteRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecetteService {
    private final RecetteRepository repository;
    private final LookupService lookupService;
    private final AuthContext authContext;

    public List<Recette> findAll() {
        return repository.findByExploitationId(authContext.currentExploitationId());
    }

    public Recette findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette introuvable : " + id));
    }

    public Recette create(RecetteRequest request) {
        Recette recette = new Recette();
        apply(recette, request);
        recette.setExploitation(lookupService.exploitation(authContext.currentExploitationId()));
        return repository.save(recette);
    }

    public Recette update(Long id, RecetteRequest request) {
        Recette recette = findById(id);
        apply(recette, request);
        return repository.save(recette);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(Recette recette, RecetteRequest request) {
        recette.setLibelle(request.libelle());
        recette.setMontant(request.montant());
        recette.setDateRecette(request.dateRecette());
        recette.setQuantite(request.quantite());
        recette.setUnite(request.unite());
        recette.setPrixUnitaire(request.prixUnitaire());
        recette.setCulture(lookupService.culture(request.cultureId()));
        recette.setUtilisateur(request.utilisateurId() == null ? null : lookupService.utilisateur(request.utilisateurId()));
    }
}
