package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Depense;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepenseRepository extends JpaRepository<Depense, Long> {
    @Query("select coalesce(sum(d.montant), 0) from Depense d")
    BigDecimal totalDepenses();

    @Query("select coalesce(sum(d.montant), 0) from Depense d where d.culture.id = :cultureId")
    BigDecimal totalByCultureId(@Param("cultureId") Long cultureId);
}
