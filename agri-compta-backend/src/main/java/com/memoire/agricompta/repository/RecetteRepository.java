package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Recette;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecetteRepository extends JpaRepository<Recette, Long> {
    @Query("select coalesce(sum(r.montant), 0) from Recette r")
    BigDecimal totalRecettes();

    @Query("select coalesce(sum(r.montant), 0) from Recette r where r.culture.id = :cultureId")
    BigDecimal totalByCultureId(@Param("cultureId") Long cultureId);
}
