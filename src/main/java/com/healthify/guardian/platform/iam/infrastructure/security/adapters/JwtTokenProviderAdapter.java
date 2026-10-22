package com.healthify.guardian.platform.iam.infrastructure.security.adapters;

import com.healthify.guardian.platform.iam.application.outboundservices.SessionToken;
import com.healthify.guardian.platform.iam.application.outboundservices.SessionTokenService;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements {@link SessionTokenService} with JWTs signed with RS256. They are the Published Language of IAM: the
 * {@code sub} claim is the user account id, and any context can check the signature with the public key alone.
 */
@Component
public class JwtTokenProviderAdapter implements SessionTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String issuer;
    private final Duration lifetime;

    public JwtTokenProviderAdapter(JwtEncoder jwtEncoder,
                                   JwtDecoder jwtDecoder,
                                   @Value("${iam.jwt.issuer:guardian-plus-platform}") String issuer,
                                   @Value("${iam.jwt.ttl-minutes:480}") long lifetimeMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.issuer = issuer;
        this.lifetime = Duration.ofMinutes(lifetimeMinutes);
    }

    @Override
    public SessionToken issue(UserAccount account) {
        var now = Instant.now();
        var expiresAt = now.plus(lifetime);
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(account.getId().value().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", account.getEmail().value())
                .build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new SessionToken(token, expiresAt);
    }

    @Override
    public Optional<UserAccountId> resolveUserAccountId(String accessToken) {
        try {
            return Optional.of(new UserAccountId(UUID.fromString(jwtDecoder.decode(accessToken).getSubject())));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
