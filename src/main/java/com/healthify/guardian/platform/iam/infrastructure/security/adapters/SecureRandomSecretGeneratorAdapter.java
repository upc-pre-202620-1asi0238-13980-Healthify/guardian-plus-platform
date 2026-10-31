package com.healthify.guardian.platform.iam.infrastructure.security.adapters;

import com.healthify.guardian.platform.iam.domain.services.SecretGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implements {@link SecretGenerator} on {@link SecureRandom}, whose output cannot be predicted from earlier codes
 * (unlike {@code java.util.Random}).
 */
@Component
public class SecureRandomSecretGeneratorAdapter implements SecretGenerator {

    private static final int OPAQUE_TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateNumericCode(int digits) {
        if (digits < 1 || digits > 9) {
            throw new IllegalArgumentException("digits must be between 1 and 9");
        }
        var bound = (int) Math.pow(10, digits);
        return String.format("%0" + digits + "d", random.nextInt(bound));
    }

    @Override
    public String generateOpaqueToken() {
        var bytes = new byte[OPAQUE_TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
