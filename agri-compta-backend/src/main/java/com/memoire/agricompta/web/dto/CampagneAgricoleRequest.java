package com.memoire.agricompta.web.dto;

import com.memoire.agricompta.domain.enums.StatutCampagne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CampagneAgricoleRequest(
        @NotBlank String nom,
        @NotNull LocalDate dateDebut,
        LocalDate dateFin,
        StatutCampagne statut
) {
}
