package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Recolte;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecolteRepository extends JpaRepository<Recolte, Long> {
    @Query("select coalesce(sum(r.quantite), 0) from Recolte r where r.culture.id = :cultureId")
    BigDecimal totalQuantiteByCultureId(@Param("cultureId") Long cultureId);
}
