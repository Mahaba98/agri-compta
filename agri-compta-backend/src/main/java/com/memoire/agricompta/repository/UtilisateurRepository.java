package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Utilisateur;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    List<Utilisateur> findByExploitationId(Long exploitationId);
}
