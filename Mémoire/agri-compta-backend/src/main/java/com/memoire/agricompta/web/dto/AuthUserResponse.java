package com.memoire.agricompta.web.dto;

import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.domain.enums.Role;

public record AuthUserResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        Role role,
        ExploitationSummaryResponse exploitation
) {
    public static AuthUserResponse from(Utilisateur utilisateur) {
        return new AuthUserResponse(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getRole(),
                ExploitationSummaryResponse.from(utilisateur.getExploitation()));
    }
}
