package com.memoire.agricompta.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecetteRequest(
        @NotBlank String libelle,
        @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
        @NotNull LocalDate dateRecette,
        @DecimalMin(value = "0.01") BigDecimal quantite,
        @Pattern(regexp = "KG|G|T|L|ML|SAC|BIDON|BOTTE|CAISSE|UNITE") String unite,
        @DecimalMin(value = "0.01") BigDecimal prixUnitaire,
        @NotNull Long cultureId,
        Long utilisateurId
) {
}
