package com.memoire.agricompta.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RapportSyntheseResponse(
        LocalDate dateDebut,
        LocalDate dateFin,
        Long campagneId,
        Long cultureId,
        BigDecimal totalDepenses,
        BigDecimal totalRecettes,
        BigDecimal benefice,
        BigDecimal totalQuantiteRecoltee,
        long nombreOperations,
        long nombreRecoltes,
        BigDecimal valeurStock,
        long nombreStocksFaibles,
        List<RapportCultureResponse> cultures
) {
}
