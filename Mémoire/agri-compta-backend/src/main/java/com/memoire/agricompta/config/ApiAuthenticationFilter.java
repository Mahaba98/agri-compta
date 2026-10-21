package com.memoire.agricompta.config;

import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.repository.UtilisateurRepository;
import com.memoire.agricompta.service.AuthTokenService;
import com.memoire.agricompta.service.AuthTokenService.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private final AuthTokenService authTokenService;
    private final ApiPermissions permissions;
    private final UtilisateurRepository utilisateurRepository;

    public ApiAuthenticationFilter(AuthTokenService authTokenService, ApiPermissions permissions,
            UtilisateurRepository utilisateurRepository) {
        this.authTokenService = authTokenService;
        this.permissions = permissions;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")
                || path.equals("/api/auth/login")
                || HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<AuthenticatedUser> authenticatedUser = authTokenService.validate(readBearerToken(request));
        if (authenticatedUser.isEmpty()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Connexion requise.");
            return;
        }

        AuthenticatedUser identity = authenticatedUser.get();
        Optional<Utilisateur> account = utilisateurRepository.findById(identity.id());
        if (account.isEmpty() || !isCurrentSession(account.get(), identity)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Session expirée. Veuillez vous reconnecter.");
            return;
        }

        if (!permissions.allows(identity.role(), request.getMethod(), path)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Votre rôle ne permet pas cette action.");
            return;
        }

        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser.get());
        filterChain.doFilter(request, response);
    }

    private boolean isCurrentSession(Utilisateur account, AuthenticatedUser identity) {
        return account.isActif() && account.getExploitation() != null && account.getExploitation().isActif()
                && account.getRole() == identity.role()
                && Objects.equals(account.getEmail(), identity.email())
                && Objects.equals(account.getExploitation().getId(), identity.exploitationId());
    }

    private String readBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getWriter().write("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
