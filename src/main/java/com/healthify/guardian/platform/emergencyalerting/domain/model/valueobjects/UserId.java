package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable reference to a Care Circle member, as identified by the {@code IAM} bounded context.
 *
 * @param value the referenced user identifier
 */
public record UserId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "user.id.invalid";

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
