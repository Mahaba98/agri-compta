package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.MouvementStock;
import com.memoire.agricompta.domain.entity.ProduitStock;
import com.memoire.agricompta.domain.enums.TypeMouvement;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.MouvementStockRepository;
import com.memoire.agricompta.repository.ProduitStockRepository;
import com.memoire.agricompta.web.dto.MouvementStockRequest;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MouvementStockService {
    private final MouvementStockRepository repository;
    private final ProduitStockRepository produitStockRepository;
    private final LookupService lookupService;

    public List<MouvementStock> findAll() {
        return repository.findAll();
    }

    public MouvementStock findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mouvement de stock introuvable : " + id));
    }

    @Transactional
    public MouvementStock create(MouvementStockRequest request) {
        ProduitStock produit = lookupService.produitStock(request.produitStockId());
        applyStockRule(produit, request);

        MouvementStock mouvement = new MouvementStock();
        mouvement.setTypeMouvement(request.typeMouvement());
        mouvement.setQuantite(request.quantite());
        mouvement.setPrixUnitaire(request.prixUnitaire());
        mouvement.setDateMouvement(request.dateMouvement());
        mouvement.setMotif(request.motif());
        mouvement.setProduitStock(produit);
        mouvement.setCulture(request.cultureId() == null ? null : lookupService.culture(request.cultureId()));
        mouvement.setOperationAgricole(request.operationAgricoleId() == null ? null : lookupService.operation(request.operationAgricoleId()));
        mouvement.setUtilisateur(request.utilisateurId() == null ? null : lookupService.utilisateur(request.utilisateurId()));

        produitStockRepository.save(produit);
        return repository.save(mouvement);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void applyStockRule(ProduitStock produit, MouvementStockRequest request) {
        BigDecimal quantiteActuelle = produit.getQuantiteDisponible();
        BigDecimal quantite = request.quantite();

        if (request.typeMouvement() == TypeMouvement.ENTREE) {
            produit.setQuantiteDisponible(quantiteActuelle.add(quantite));
            if (request.prixUnitaire() != null) {
                produit.setPrixUnitaireMoyen(request.prixUnitaire());
            }
            return;
        }

        if (request.typeMouvement() == TypeMouvement.SORTIE) {
            if (quantiteActuelle.compareTo(quantite) < 0) {
                throw new BusinessException("Stock insuffisant pour effectuer cette sortie.");
            }
            produit.setQuantiteDisponible(quantiteActuelle.subtract(quantite));
            return;
        }

        produit.setQuantiteDisponible(quantite);
    }
}
