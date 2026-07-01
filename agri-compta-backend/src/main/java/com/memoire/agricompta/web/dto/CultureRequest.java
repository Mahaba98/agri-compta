package com.memoire.agricompta.web.dto;

import com.memoire.agricompta.domain.enums.StatutCulture;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CultureRequest(
        @NotBlank String nom,
        String variete,
        @NotNull @DecimalMin(value = "0.01") BigDecimal surfaceHa,
        LocalDate dateSemis,
        LocalDate dateRecoltePrevue,
        LocalDate dateRecolteReelle,
        StatutCulture statut,
        @NotNull Long campagneId,
        @NotNull Long parcelleId
) {
}
