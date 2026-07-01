package com.memoire.agricompta.web.dto;

import com.memoire.agricompta.domain.enums.TypeProduit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record ProduitStockRequest(
        @NotBlank String nom,
        @NotNull TypeProduit typeProduit,
        @NotBlank @Pattern(regexp = "KG|G|T|L|ML|SAC|BIDON|BOTTE|CAISSE|UNITE") String unite,
        @NotNull @DecimalMin(value = "0.00") BigDecimal quantiteDisponible,
        @NotNull @DecimalMin(value = "0.00") BigDecimal seuilAlerte,
        @DecimalMin(value = "0.00") BigDecimal prixUnitaireMoyen
) {
}
