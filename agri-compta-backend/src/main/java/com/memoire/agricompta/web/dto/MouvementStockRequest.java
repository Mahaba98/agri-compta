package com.memoire.agricompta.web.dto;

import com.memoire.agricompta.domain.enums.TypeMouvement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MouvementStockRequest(
        @NotNull TypeMouvement typeMouvement,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantite,
        @DecimalMin(value = "0.00") BigDecimal prixUnitaire,
        @NotNull LocalDate dateMouvement,
        String motif,
        @NotNull Long produitStockId,
        Long cultureId,
        Long operationAgricoleId,
        Long utilisateurId
) {
}
