package com.healthify.guardian.platform.iam.infrastructure.security.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * RSA key pair of the session tokens: IAM signs with the private key and every request is checked with the public
 * key. Both come as PEM from {@code iam.jwt.private-key} (PKCS#8) and {@code iam.jwt.public-key} (X.509). Without
 * them a random pair is generated at startup, which is fine for development but logs everyone out on each restart.
 */
@Slf4j
@Configuration
public class JwtConfiguration {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final String issuer;

    public JwtConfiguration(@Value("${iam.jwt.public-key:}") String publicKeyPem,
                            @Value("${iam.jwt.private-key:}") String privateKeyPem,
                            @Value("${iam.jwt.issuer:guardian-plus-platform}") String issuer) {
        this.issuer = issuer;
        if (publicKeyPem.isBlank() || privateKeyPem.isBlank()) {
            log.warn("iam.jwt.public-key / iam.jwt.private-key are not set: using a random RSA key pair, so session "
                    + "tokens stop working after a restart. Set IAM_JWT_PUBLIC_KEY and IAM_JWT_PRIVATE_KEY outside development.");
            var keyPair = generateKeyPair();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        } else {
            this.publicKey = RsaKeyConverters.x509().convert(pem(publicKeyPem));
            this.privateKey = RsaKeyConverters.pkcs8().convert(pem(privateKeyPem));
        }
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        var rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        var decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        // Checks the signature, the expiration and that the token was issued by this IAM
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    private static ByteArrayInputStream pem(String value) {
        // Environment variables often carry the PEM on one line with literal "\n" separators
        return new ByteArrayInputStream(value.replace("\\n", "\n").strip().getBytes(StandardCharsets.UTF_8));
    }

    private static java.security.KeyPair generateKeyPair() {
        try {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA is not available", e);
        }
    }
}
