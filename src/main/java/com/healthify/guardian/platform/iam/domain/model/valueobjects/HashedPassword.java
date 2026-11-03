package com.healthify.guardian.platform.iam.domain.model.valueobjects;

/**
 * Irreversible hash of a user's password. Only the {@code PasswordHasher} builds it from a plain-text password,
 * so the domain never holds the password itself.
 *
 * @param hash the encoded hash (e.g. BCrypt)
 */
public record HashedPassword(String hash) {

    private static final String INVALID_MESSAGE_KEY = "user-account.password-hash.invalid";

    public HashedPassword {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    /** Keeps the hash out of logs and error messages. */
    @Override
    public String toString() {
        return "HashedPassword[****]";
    }
}
