package com.memoire.agricompta.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.memoire.agricompta.domain.entity.Exploitation;
import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.domain.enums.Role;
import com.memoire.agricompta.repository.UtilisateurRepository;
import com.memoire.agricompta.service.AuthTokenService;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiAuthenticationFilterTest {
    private final AuthTokenService tokens = new AuthTokenService("test-only-secret");
    private final UtilisateurRepository users = mock(UtilisateurRepository.class);
    private final ApiAuthenticationFilter filter = new ApiAuthenticationFilter(tokens, new ApiPermissions(), users);

    private Utilisateur account(Role role) {
        Exploitation farm = new Exploitation(); farm.setId(10L); farm.setActif(true);
        Utilisateur user = new Utilisateur(); user.setId(1L); user.setRole(role);
        user.setEmail("role@example.test"); user.setActif(true); user.setExploitation(farm);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        return user;
    }

    private MockHttpServletResponse call(String method, String path, String token, boolean allowed) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        if (token != null) request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain, times(allowed ? 1 : 0)).doFilter(request, response);
        return response;
    }

    @Test
    void farmerCanReadReportsButAccountantCannotEditAPlot() throws Exception {
        assertEquals(200, call("GET", "/api/rapports/synthese", tokens.generate(account(Role.AGRICULTEUR)), true).getStatus());
        assertEquals(403, call("PUT", "/api/parcelles/1", tokens.generate(account(Role.COMPTABLE)), false).getStatus());
        assertEquals(403, call("POST", "/api/recoltes", tokens.generate(account(Role.ADMIN)), false).getStatus());
    }

    @Test
    void removedDisabledAndChangedAccountsInvalidateExistingTokens() throws Exception {
        Utilisateur user = account(Role.AGRICULTEUR);
        String token = tokens.generate(user);
        user.setActif(false);
        assertEquals(401, call("GET", "/api/dashboard", token, false).getStatus());
        user.setActif(true); user.setRole(Role.ADMIN);
        assertEquals(401, call("GET", "/api/dashboard", token, false).getStatus());
        user.setRole(Role.AGRICULTEUR); user.getExploitation().setActif(false);
        assertEquals(401, call("GET", "/api/dashboard", token, false).getStatus());
        when(users.findById(1L)).thenReturn(Optional.empty());
        assertEquals(401, call("GET", "/api/dashboard", token, false).getStatus());
    }

    @Test
    void missingAndMalformedTokensAreRefused() throws Exception {
        assertEquals(401, call("GET", "/api/dashboard", null, false).getStatus());
        assertEquals(401, call("GET", "/api/dashboard", "invalid.signature", false).getStatus());
    }

    @Test
    void generatedSecretsAreNotSharedBetweenServerInstances() {
        Utilisateur user = account(Role.AGRICULTEUR);
        AuthTokenService first = new AuthTokenService("");
        AuthTokenService second = new AuthTokenService("");
        String token = first.generate(user);
        assertTrue(first.validate(token).isPresent());
        assertTrue(second.validate(token).isEmpty());
    }
}
