package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.MouvementStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
    List<MouvementStock> findByExploitationId(Long exploitationId);
}
