package com.healthify.guardian.platform.iam.infrastructure.security.adapters;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Implements {@link SecretHasher} with HMAC-SHA256 under a server-side key.
 *
 * <p>Deterministic on purpose: the same token always gives the same hash, which is what lets a password reset token
 * be looked up by its hash (BCrypt's random salt would not). The key keeps the 6-digit OTP codes safe if the
 * database leaks: without it, a plain SHA-256 of a code could be reversed by trying all one million codes.</p>
 */
@Slf4j
@Component
public class HmacSha256SecretHasherAdapter implements SecretHasher {

    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec key;

    public HmacSha256SecretHasherAdapter(@Value("${iam.secrets.hmac-key:}") String base64Key) {
        byte[] keyBytes;
        if (base64Key == null || base64Key.isBlank()) {
            log.warn("iam.secrets.hmac-key is not set: using a random key, so pending codes and reset tokens stop "
                    + "working after a restart. Set IAM_SECRETS_HMAC_KEY outside development.");
            keyBytes = new byte[32];
            new SecureRandom().nextBytes(keyBytes);
        } else {
            keyBytes = Base64.getDecoder().decode(base64Key.strip());
        }
        this.key = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    @Override
    public HashedSecret hash(String rawSecret) {
        try {
            var mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return new HashedSecret(HexFormat.of().formatHex(mac.doFinal(rawSecret.getBytes(StandardCharsets.UTF_8))));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }

    @Override
    public boolean matches(String rawSecret, HashedSecret hash) {
        if (rawSecret == null || hash == null) {
            return false;
        }
        // Constant-time comparison, so the response time does not reveal how many characters matched
        return MessageDigest.isEqual(hash(rawSecret).hash().getBytes(StandardCharsets.UTF_8),
                hash.hash().getBytes(StandardCharsets.UTF_8));
    }
}
