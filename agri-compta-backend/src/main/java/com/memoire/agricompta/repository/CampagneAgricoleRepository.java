package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.CampagneAgricole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampagneAgricoleRepository extends JpaRepository<CampagneAgricole, Long> {
    List<CampagneAgricole> findByExploitationId(Long exploitationId);
}
