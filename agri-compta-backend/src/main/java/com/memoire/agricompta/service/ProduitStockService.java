package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.ProduitStock;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.ProduitStockRepository;
import com.memoire.agricompta.web.dto.ProduitStockRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProduitStockService {
    private final ProduitStockRepository repository;
    private final AuthContext authContext;
    private final LookupService lookupService;

    public List<ProduitStock> findAll() {
        return repository.findByExploitationId(authContext.currentExploitationId());
    }

    public List<ProduitStock> findProduitsEnAlerte() {
        return repository.findProduitsEnAlerteByExploitationId(authContext.currentExploitationId());
    }

    public ProduitStock findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit en stock introuvable : " + id));
    }

    public ProduitStock create(ProduitStockRequest request) {
        ProduitStock produit = new ProduitStock();
        apply(produit, request);
        produit.setExploitation(lookupService.exploitation(authContext.currentExploitationId()));
        return repository.save(produit);
    }

    public ProduitStock update(Long id, ProduitStockRequest request) {
        ProduitStock produit = findById(id);
        apply(produit, request);
        return repository.save(produit);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(ProduitStock produit, ProduitStockRequest request) {
        produit.setNom(request.nom());
        produit.setTypeProduit(request.typeProduit());
        produit.setUnite(request.unite());
        produit.setQuantiteDisponible(request.quantiteDisponible());
        produit.setSeuilAlerte(request.seuilAlerte());
        produit.setPrixUnitaireMoyen(request.prixUnitaireMoyen());
    }
}
