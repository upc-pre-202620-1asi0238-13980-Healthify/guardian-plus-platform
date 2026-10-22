package com.healthify.guardian.platform.iam.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier of a {@code PasswordResetToken} aggregate.
 *
 * @param value the underlying UUID
 */
public record PasswordResetTokenId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "password-reset-token.id.invalid";

    public PasswordResetTokenId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static PasswordResetTokenId generate() {
        return new PasswordResetTokenId(UUID.randomUUID());
    }
}
