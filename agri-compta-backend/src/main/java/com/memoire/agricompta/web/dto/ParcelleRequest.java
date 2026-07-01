package com.memoire.agricompta.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ParcelleRequest(
        @NotBlank String nom,
        @NotNull @DecimalMin(value = "0.01") BigDecimal superficieHa,
        String localisation,
        String typeSol,
        String typeIrrigation,
        String description
) {
}
