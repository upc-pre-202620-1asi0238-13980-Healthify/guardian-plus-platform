package com.healthify.guardian.platform.iam.infrastructure.security.adapters;

import com.healthify.guardian.platform.iam.application.outboundservices.GoogleIdentity;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Signs tokens shaped like Google's with a local key, standing in for Google's published keys.
 */
class NimbusGoogleIdTokenVerifierAdapterTest {

    private static final String CLIENT_ID = "web-client.apps.googleusercontent.com";

    private final RSAPublicKey publicKey;
    private final NimbusJwtEncoder encoder;

    NimbusGoogleIdTokenVerifierAdapterTest() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var keyPair = generator.generateKeyPair();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        var rsaKey = new RSAKey.Builder(publicKey).privateKey((RSAPrivateKey) keyPair.getPrivate()).build();
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    private NimbusGoogleIdTokenVerifierAdapter verifier(String clientIds) {
        return new NimbusGoogleIdTokenVerifierAdapter(clientIds, NimbusJwtDecoder.withPublicKey(publicKey).build());
    }

    private String token(Consumer<JwtClaimsSet.Builder> customizer) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("https://accounts.google.com")
                .audience(List.of(CLIENT_ID))
                .subject("google-1")
                .claim("email", "ana@guardian.pe")
                .claim("email_verified", true)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(1)));
        customizer.accept(claims);
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    @Test
    void aValidTokenGivesTheGoogleAccount() {
        assertThat(verifier(CLIENT_ID).verify(token(claims -> { })))
                .contains(new GoogleIdentity("google-1", "ana@guardian.pe"));
    }

    @Test
    void acceptsAnyOfTheConfiguredClientsAndBothIssuerForms() {
        var token = token(claims -> claims.issuer("accounts.google.com"));

        assertThat(verifier("other-client, " + CLIENT_ID).verify(token)).isPresent();
    }

    @Test
    void rejectsATokenForAnotherClient() {
        assertThat(verifier("other-client").verify(token(claims -> { }))).isEmpty();
    }

    @Test
    void rejectsATokenFromAnotherIssuer() {
        assertThat(verifier(CLIENT_ID).verify(token(claims -> claims.issuer("https://evil.example.com")))).isEmpty();
    }

    @Test
    void rejectsAnExpiredToken() {
        var past = Instant.now().minus(Duration.ofHours(2));

        assertThat(verifier(CLIENT_ID).verify(token(claims -> claims.issuedAt(past).expiresAt(past.plusSeconds(60))))).isEmpty();
    }

    @Test
    void rejectsAnUnverifiedEmail() {
        assertThat(verifier(CLIENT_ID).verify(token(claims -> claims.claim("email_verified", false)))).isEmpty();
    }

    @Test
    void rejectsEverythingWithoutAConfiguredClient() {
        assertThat(verifier("").verify(token(claims -> { }))).isEmpty();
        assertThat(verifier(CLIENT_ID).verify("not-a-jwt")).isEmpty();
    }
}
