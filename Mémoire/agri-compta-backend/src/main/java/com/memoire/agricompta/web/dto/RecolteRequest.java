package com.memoire.agricompta.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecolteRequest(
        @NotNull LocalDate dateRecolte,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantite,
        @NotBlank @Pattern(regexp = "KG|G|T|L|ML|SAC|BIDON|BOTTE|CAISSE|UNITE") String unite,
        @DecimalMin(value = "0.00") BigDecimal prixUnitaire,
        @DecimalMin(value = "0.00") BigDecimal montantTotal,
        String observation,
        @NotNull Long cultureId,
        Long utilisateurId
) {
}
