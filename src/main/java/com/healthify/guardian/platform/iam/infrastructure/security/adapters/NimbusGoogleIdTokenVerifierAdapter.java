package com.healthify.guardian.platform.iam.infrastructure.security.adapters;

import com.healthify.guardian.platform.iam.application.outboundservices.GoogleIdTokenVerifier;
import com.healthify.guardian.platform.iam.application.outboundservices.GoogleIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implements {@link GoogleIdTokenVerifier} with Nimbus, checking the token against Google's public keys, which the
 * decoder downloads and caches. The audience must be one of the Web OAuth client ids in {@code iam.google.client-ids}
 * (comma separated): the app asks Google for a token for that client. The client secret is not needed.
 *
 * <p>The decoder is not a bean on purpose: the resource server must keep using IAM's own {@code JwtDecoder}.</p>
 */
@Slf4j
@Component
public class NimbusGoogleIdTokenVerifierAdapter implements GoogleIdTokenVerifier {

    private static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";
    // Google signs with either form of its issuer
    private static final Set<String> ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final Set<String> clientIds;
    private final JwtDecoder decoder;

    @Autowired
    public NimbusGoogleIdTokenVerifierAdapter(@Value("${iam.google.client-ids:}") String clientIds) {
        this(clientIds, NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build());
    }

    // Lets tests check tokens signed with their own key
    NimbusGoogleIdTokenVerifierAdapter(String clientIds, NimbusJwtDecoder decoder) {
        this.clientIds = Arrays.stream(clientIds.split(","))
                .map(String::strip)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        if (this.clientIds.isEmpty()) {
            log.warn("iam.google.client-ids is not set: every Google sign-in is rejected. Set IAM_GOOGLE_CLIENT_IDS.");
        }
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(), claim("iss", jwt -> ISSUERS.contains(jwt.getClaimAsString("iss"))),
                claim("aud", jwt -> jwt.getAudience() != null && jwt.getAudience().stream().anyMatch(this.clientIds::contains))));
        this.decoder = decoder;
    }

    @Override
    public Optional<GoogleIdentity> verify(String idToken) {
        if (clientIds.isEmpty() || idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }
        Jwt jwt;
        try {
            jwt = decoder.decode(idToken);
        } catch (JwtException e) {
            log.debug("Google ID token rejected: {}", e.getMessage());
            return Optional.empty();
        }
        var email = jwt.getClaimAsString("email");
        // Google sends email_verified as a boolean, older tokens as the string "true"
        var emailVerified = "true".equalsIgnoreCase(String.valueOf(jwt.getClaims().get("email_verified")));
        if (jwt.getSubject() == null || email == null || !emailVerified) {
            return Optional.empty();
        }
        return Optional.of(new GoogleIdentity(jwt.getSubject(), email));
    }

    private static OAuth2TokenValidator<Jwt> claim(String name, Predicate<Jwt> valid) {
        return jwt -> valid.test(jwt)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid " + name + " claim", null));
    }
}
