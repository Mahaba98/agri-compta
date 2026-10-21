package com.memoire.agricompta.web.dto;

import java.math.BigDecimal;

public record CultureRentabiliteResponse(
        Long cultureId,
        String culture,
        BigDecimal surfaceHa,
        BigDecimal totalDepenses,
        BigDecimal totalOperations,
        BigDecimal coutTotal,
        BigDecimal recetteTotale,
        BigDecimal benefice,
        BigDecimal coutParHectare,
        BigDecimal margeParHectare
) {
}
