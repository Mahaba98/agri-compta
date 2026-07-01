package com.memoire.agricompta.web.dto;

public record LoginResponse(
        String token,
        AuthUserResponse utilisateur
) {
}
