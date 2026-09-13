package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.CampagneAgricole;
import com.memoire.agricompta.domain.entity.CategorieDepense;
import com.memoire.agricompta.domain.entity.Culture;
import com.memoire.agricompta.domain.entity.Exploitation;
import com.memoire.agricompta.domain.entity.OperationAgricole;
import com.memoire.agricompta.domain.entity.Parcelle;
import com.memoire.agricompta.domain.entity.ProduitStock;
import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.CampagneAgricoleRepository;
import com.memoire.agricompta.repository.CategorieDepenseRepository;
import com.memoire.agricompta.repository.CultureRepository;
import com.memoire.agricompta.repository.ExploitationRepository;
import com.memoire.agricompta.repository.OperationAgricoleRepository;
import com.memoire.agricompta.repository.ParcelleRepository;
import com.memoire.agricompta.repository.ProduitStockRepository;
import com.memoire.agricompta.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LookupService {
    private final CampagneAgricoleRepository campagneRepository;
    private final ParcelleRepository parcelleRepository;
    private final CultureRepository cultureRepository;
    private final CategorieDepenseRepository categorieRepository;
    private final OperationAgricoleRepository operationRepository;
    private final ProduitStockRepository produitStockRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ExploitationRepository exploitationRepository;

    public CampagneAgricole campagne(Long id) {
        return campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable : " + id));
    }

    public Parcelle parcelle(Long id) {
        return parcelleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle introuvable : " + id));
    }

    public Culture culture(Long id) {
        return cultureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Culture introuvable : " + id));
    }

    public CategorieDepense categorie(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable : " + id));
    }

    public OperationAgricole operation(Long id) {
        return operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation agricole introuvable : " + id));
    }

    public ProduitStock produitStock(Long id) {
        return produitStockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit en stock introuvable : " + id));
    }

    public Utilisateur utilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    public Exploitation exploitation(Long id) {
        return exploitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exploitation introuvable : " + id));
    }
}
