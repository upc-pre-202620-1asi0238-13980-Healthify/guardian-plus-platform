package com.healthify.guardian.platform.iam.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier of a {@code UserAccount} aggregate.
 *
 * @param value the underlying UUID
 */
public record UserAccountId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "user-account.id.invalid";

    public UserAccountId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static UserAccountId generate() {
        return new UserAccountId(UUID.randomUUID());
    }
}
