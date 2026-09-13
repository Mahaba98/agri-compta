package com.memoire.agricompta.config;

import com.memoire.agricompta.domain.entity.CategorieDepense;
import com.memoire.agricompta.domain.entity.Exploitation;
import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.domain.enums.Role;
import com.memoire.agricompta.repository.CampagneAgricoleRepository;
import com.memoire.agricompta.repository.CategorieDepenseRepository;
import com.memoire.agricompta.repository.CultureRepository;
import com.memoire.agricompta.repository.DepenseRepository;
import com.memoire.agricompta.repository.ExploitationRepository;
import com.memoire.agricompta.repository.MouvementStockRepository;
import com.memoire.agricompta.repository.OperationAgricoleRepository;
import com.memoire.agricompta.repository.ParcelleRepository;
import com.memoire.agricompta.repository.ProduitStockRepository;
import com.memoire.agricompta.repository.RecetteRepository;
import com.memoire.agricompta.repository.RecolteRepository;
import com.memoire.agricompta.repository.UtilisateurRepository;
import com.memoire.agricompta.service.PasswordService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initCategories(CategorieDepenseRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            List<String> categories = List.of(
                    "Semences",
                    "Engrais",
                    "Produits phytosanitaires",
                    "Main-d'oeuvre",
                    "Carburant",
                    "Irrigation",
                    "Location materiel",
                    "Autres charges"
            );
            for (String nom : categories) {
                CategorieDepense categorie = new CategorieDepense();
                categorie.setNom(nom);
                repository.save(categorie);
            }
        };
    }

    @Bean
    CommandLineRunner initAdminUser(
            UtilisateurRepository repository,
            ExploitationRepository exploitationRepository,
            PasswordService passwordService) {
        return args -> {
            Exploitation exploitation = defaultExploitation(exploitationRepository);
            if (repository.findByEmailIgnoreCase("admin@touba-agoro.local").isPresent()) {
                return;
            }

            Utilisateur admin = new Utilisateur();
            admin.setNom("Administrateur");
            admin.setPrenom("Principal");
            admin.setEmail("admin@touba-agoro.local");
            admin.setMotDePasse(passwordService.hash("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            admin.setExploitation(exploitation);
            repository.save(admin);
        };
    }

    @Bean
    CommandLineRunner initDefaultExploitation(
            ExploitationRepository exploitationRepository,
            UtilisateurRepository utilisateurRepository,
            CampagneAgricoleRepository campagneRepository,
            ParcelleRepository parcelleRepository,
            CultureRepository cultureRepository,
            OperationAgricoleRepository operationRepository,
            RecolteRepository recolteRepository,
            DepenseRepository depenseRepository,
            RecetteRepository recetteRepository,
            ProduitStockRepository produitStockRepository,
            MouvementStockRepository mouvementStockRepository) {
        return args -> {
            Exploitation exploitation = defaultExploitation(exploitationRepository);

            utilisateurRepository.findAll().stream()
                    .filter(utilisateur -> utilisateur.getExploitation() == null)
                    .forEach(utilisateur -> {
                        utilisateur.setExploitation(exploitation);
                        utilisateurRepository.save(utilisateur);
                    });
            campagneRepository.findAll().stream()
                    .filter(campagne -> campagne.getExploitation() == null)
                    .forEach(campagne -> {
                        campagne.setExploitation(exploitation);
                        campagneRepository.save(campagne);
                    });
            parcelleRepository.findAll().stream()
                    .filter(parcelle -> parcelle.getExploitation() == null)
                    .forEach(parcelle -> {
                        parcelle.setExploitation(exploitation);
                        parcelleRepository.save(parcelle);
                    });
            cultureRepository.findAll().stream()
                    .filter(culture -> culture.getExploitation() == null)
                    .forEach(culture -> {
                        culture.setExploitation(exploitation);
                        cultureRepository.save(culture);
                    });
            operationRepository.findAll().stream()
                    .filter(operation -> operation.getExploitation() == null)
                    .forEach(operation -> {
                        operation.setExploitation(exploitation);
                        operationRepository.save(operation);
                    });
            recolteRepository.findAll().stream()
                    .filter(recolte -> recolte.getExploitation() == null)
                    .forEach(recolte -> {
                        recolte.setExploitation(exploitation);
                        recolteRepository.save(recolte);
                    });
            depenseRepository.findAll().stream()
                    .filter(depense -> depense.getExploitation() == null)
                    .forEach(depense -> {
                        depense.setExploitation(exploitation);
                        depenseRepository.save(depense);
                    });
            recetteRepository.findAll().stream()
                    .filter(recette -> recette.getExploitation() == null)
                    .forEach(recette -> {
                        recette.setExploitation(exploitation);
                        recetteRepository.save(recette);
                    });
            produitStockRepository.findAll().stream()
                    .filter(produit -> produit.getExploitation() == null)
                    .forEach(produit -> {
                        produit.setExploitation(exploitation);
                        produitStockRepository.save(produit);
                    });
            mouvementStockRepository.findAll().stream()
                    .filter(mouvement -> mouvement.getExploitation() == null)
                    .forEach(mouvement -> {
                        mouvement.setExploitation(exploitation);
                        mouvementStockRepository.save(mouvement);
                    });
        };
    }

    private Exploitation defaultExploitation(ExploitationRepository repository) {
        return repository.findByNomIgnoreCase("Touba Agoro").orElseGet(() -> {
            Exploitation exploitation = new Exploitation();
            exploitation.setNom("Touba Agoro");
            exploitation.setLogoUrl("logo-touba-agoro.jpg");
            exploitation.setLocalisation("Senegal");
            exploitation.setDescription("Exploitation agricole par défaut");
            exploitation.setActif(true);
            return repository.save(exploitation);
        });
    }
}
