package com.memoire.agricompta.web.dto;

import com.memoire.agricompta.domain.enums.TypeOperation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OperationAgricoleRequest(
        @NotNull TypeOperation typeOperation,
        @NotNull LocalDate dateOperation,
        String description,
        @DecimalMin(value = "0.00") BigDecimal coutMainOeuvre,
        @NotNull Long cultureId,
        Long utilisateurId
) {
}
