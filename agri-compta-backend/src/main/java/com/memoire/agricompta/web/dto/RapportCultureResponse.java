package com.memoire.agricompta.web.dto;

import java.math.BigDecimal;

public record RapportCultureResponse(
        Long cultureId,
        String culture,
        String campagne,
        String parcelle,
        BigDecimal surfaceHa,
        BigDecimal totalDepenses,
        BigDecimal totalRecettes,
        BigDecimal benefice,
        BigDecimal quantiteRecoltee,
        String unite,
        long nombreOperations,
        long nombreRecoltes
) {
}
