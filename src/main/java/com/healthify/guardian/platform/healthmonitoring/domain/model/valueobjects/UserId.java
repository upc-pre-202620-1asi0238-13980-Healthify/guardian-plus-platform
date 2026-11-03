package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable reference to the authenticated user that requested a health report, owned by the {@code IAM} bounded context.
 *
 * @param value the underlying UUID
 */
public record UserId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "user.id.invalid";

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
