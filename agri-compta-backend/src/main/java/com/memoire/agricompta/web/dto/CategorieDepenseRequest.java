package com.memoire.agricompta.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CategorieDepenseRequest(
        @NotBlank String nom,
        String description
) {
}
