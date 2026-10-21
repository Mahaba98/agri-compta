package com.memoire.agricompta.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseRequest(
        @NotBlank String libelle,
        @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
        @NotNull LocalDate dateDepense,
        String modePaiement,
        String referencePiece,
        @NotNull Long cultureId,
        @NotNull Long categorieId,
        Long operationAgricoleId,
        Long utilisateurId
) {
}
