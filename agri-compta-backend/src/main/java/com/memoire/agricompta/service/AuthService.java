package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.repository.UtilisateurRepository;
import com.memoire.agricompta.web.dto.AuthUserResponse;
import com.memoire.agricompta.web.dto.LoginRequest;
import com.memoire.agricompta.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordService passwordService;
    private final AuthTokenService authTokenService;

    public LoginResponse login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException("Email ou mot de passe incorrect."));

        if (!utilisateur.isActif() || !passwordService.matches(request.motDePasse(), utilisateur.getMotDePasse())) {
            throw new BusinessException("Email ou mot de passe incorrect.");
        }

        return new LoginResponse(authTokenService.generate(utilisateur), AuthUserResponse.from(utilisateur));
    }
}
