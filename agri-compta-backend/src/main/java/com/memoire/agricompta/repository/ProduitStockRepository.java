package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.ProduitStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProduitStockRepository extends JpaRepository<ProduitStock, Long> {
    @Query("select p from ProduitStock p where p.quantiteDisponible <= p.seuilAlerte")
    List<ProduitStock> findProduitsEnAlerte();
}
