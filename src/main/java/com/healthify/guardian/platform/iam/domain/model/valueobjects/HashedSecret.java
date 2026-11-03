package com.healthify.guardian.platform.iam.domain.model.valueobjects;

/**
 * Irreversible hash of a short-lived secret: an OTP code ({@code code_hash}) or a password reset token
 * ({@code token_hash}). Only the {@code SecretHasher} builds it, so the secret itself is never stored.
 *
 * @param hash the encoded hash
 */
public record HashedSecret(String hash) {

    private static final String INVALID_MESSAGE_KEY = "hashed-secret.invalid";

    public HashedSecret {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    /** Keeps the hash out of logs and error messages. */
    @Override
    public String toString() {
        return "HashedSecret[****]";
    }
}
