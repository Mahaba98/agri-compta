package com.memoire.agricompta.web.dto;

import com.memoire.agricompta.domain.entity.ProduitStock;
import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalDepenses,
        BigDecimal totalOperations,
        BigDecimal totalRecettes,
        BigDecimal beneficeGlobal,
        List<CultureRentabiliteResponse> rentabilites,
        List<ProduitStock> stocksFaibles
) {
}
