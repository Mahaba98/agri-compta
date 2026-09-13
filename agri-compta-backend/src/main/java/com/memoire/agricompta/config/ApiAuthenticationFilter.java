package com.memoire.agricompta.config;

import com.memoire.agricompta.domain.enums.Role;
import com.memoire.agricompta.service.AuthTokenService;
import com.memoire.agricompta.service.AuthTokenService.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private final AuthTokenService authTokenService;

    public ApiAuthenticationFilter(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
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

        if (path.startsWith("/api/utilisateurs") && authenticatedUser.get().role() != Role.ADMIN) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Accès réservé à l'administrateur.");
            return;
        }

        if (path.startsWith("/api/exploitations") && authenticatedUser.get().role() != Role.ADMIN) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Accès réservé à l'administrateur.");
            return;
        }

        if (path.startsWith("/api/rapports")
                && authenticatedUser.get().role() != Role.ADMIN
                && authenticatedUser.get().role() != Role.COMPTABLE) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Accès réservé à l'administrateur et au comptable.");
            return;
        }

        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser.get());
        filterChain.doFilter(request, response);
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
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getWriter().write("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
