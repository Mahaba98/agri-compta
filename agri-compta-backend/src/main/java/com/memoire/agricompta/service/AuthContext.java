package com.memoire.agricompta.service;

import com.memoire.agricompta.config.ApiAuthenticationFilter;
import com.memoire.agricompta.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthContext {
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public AuthTokenService.AuthenticatedUser currentUser() {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request == null) {
            throw new BusinessException("Connexion requise.");
        }
        Object value = request.getAttribute(ApiAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE);
        if (value instanceof AuthTokenService.AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }
        throw new BusinessException("Connexion requise.");
    }

    public Long currentExploitationId() {
        return currentUser().exploitationId();
    }
}
