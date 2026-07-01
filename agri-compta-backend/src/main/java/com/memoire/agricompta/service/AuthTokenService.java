package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.domain.enums.Role;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public AuthTokenService(@Value("${app.auth.secret:agri-compta-local-secret-change-me}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generate(Utilisateur utilisateur) {
        long expiresAt = Instant.now().plus(8, ChronoUnit.HOURS).getEpochSecond();
        String payload = String.join("|",
                String.valueOf(utilisateur.getId()),
                utilisateur.getRole().name(),
                utilisateur.getEmail(),
                String.valueOf(expiresAt));
        String encodedPayload = base64Url(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public Optional<AuthenticatedUser> validate(String token) {
        if (token == null || !token.contains(".")) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.", 2);
        if (!sign(parts[0]).equals(parts[1])) {
            return Optional.empty();
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] values = payload.split("\\|", 4);
        if (values.length != 4) {
            return Optional.empty();
        }

        try {
            if (Long.parseLong(values[3]) < Instant.now().getEpochSecond()) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUser(
                    Long.parseLong(values[0]),
                    Role.valueOf(values[1]),
                    values[2]));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Impossible de signer le token", ex);
        }
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public record AuthenticatedUser(Long id, Role role, String email) {
    }
}
