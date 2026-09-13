package com.memoire.agricompta.web.dto;

import com.memoire.agricompta.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UtilisateurRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @Email @NotBlank String email,
        String motDePasse,
        @NotNull Role role,
        boolean actif,
        Long exploitationId
) {
}
