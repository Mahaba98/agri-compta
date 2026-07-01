package com.memoire.agricompta.config;

import com.memoire.agricompta.domain.entity.CategorieDepense;
import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.domain.enums.Role;
import com.memoire.agricompta.repository.CategorieDepenseRepository;
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
    CommandLineRunner initAdminUser(UtilisateurRepository repository, PasswordService passwordService) {
        return args -> {
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
            repository.save(admin);
        };
    }
}
