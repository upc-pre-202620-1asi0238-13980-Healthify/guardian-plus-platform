package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier of a {@code VitalSign} aggregate.
 *
 * @param value the underlying UUID
 */
public record VitalSignId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "vital-sign.id.invalid";

    public VitalSignId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static VitalSignId generate() {
        return new VitalSignId(UUID.randomUUID());
    }
}
