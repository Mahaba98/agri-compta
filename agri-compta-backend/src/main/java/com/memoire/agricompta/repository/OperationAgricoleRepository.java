package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.OperationAgricole;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OperationAgricoleRepository extends JpaRepository<OperationAgricole, Long> {
    List<OperationAgricole> findByExploitationId(Long exploitationId);

    @Query("select coalesce(sum(o.coutMainOeuvre), 0) from OperationAgricole o where o.exploitation.id = :exploitationId")
    BigDecimal totalOperationsByExploitationId(@Param("exploitationId") Long exploitationId);

    @Query("select coalesce(sum(o.coutMainOeuvre), 0) from OperationAgricole o where o.exploitation.id = :exploitationId and o.culture.id = :cultureId")
    BigDecimal totalByExploitationIdAndCultureId(@Param("exploitationId") Long exploitationId, @Param("cultureId") Long cultureId);
}
