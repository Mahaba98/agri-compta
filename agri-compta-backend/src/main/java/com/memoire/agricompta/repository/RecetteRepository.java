package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Recette;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecetteRepository extends JpaRepository<Recette, Long> {
    List<Recette> findByExploitationId(Long exploitationId);

    @Query("select coalesce(sum(r.montant), 0) from Recette r")
    BigDecimal totalRecettes();

    @Query("select coalesce(sum(r.montant), 0) from Recette r where r.exploitation.id = :exploitationId")
    BigDecimal totalRecettesByExploitationId(@Param("exploitationId") Long exploitationId);

    @Query("select coalesce(sum(r.montant), 0) from Recette r where r.culture.id = :cultureId")
    BigDecimal totalByCultureId(@Param("cultureId") Long cultureId);

    @Query("select coalesce(sum(r.montant), 0) from Recette r where r.exploitation.id = :exploitationId and r.culture.id = :cultureId")
    BigDecimal totalByExploitationIdAndCultureId(@Param("exploitationId") Long exploitationId, @Param("cultureId") Long cultureId);
}
