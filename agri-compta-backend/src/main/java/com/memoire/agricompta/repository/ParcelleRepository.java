package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Parcelle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelleRepository extends JpaRepository<Parcelle, Long> {
    List<Parcelle> findByExploitationId(Long exploitationId);
}
