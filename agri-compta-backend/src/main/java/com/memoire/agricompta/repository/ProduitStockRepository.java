package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.ProduitStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProduitStockRepository extends JpaRepository<ProduitStock, Long> {
    List<ProduitStock> findByExploitationId(Long exploitationId);

    @Query("select p from ProduitStock p where p.quantiteDisponible <= p.seuilAlerte")
    List<ProduitStock> findProduitsEnAlerte();

    @Query("select p from ProduitStock p where p.exploitation.id = :exploitationId and p.quantiteDisponible <= p.seuilAlerte")
    List<ProduitStock> findProduitsEnAlerteByExploitationId(@Param("exploitationId") Long exploitationId);
}
