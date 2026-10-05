package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier of a {@code VitalSignThreshold} aggregate.
 *
 * @param value the underlying UUID
 */
public record VitalSignThresholdId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "vital-sign-threshold.id.invalid";

    public VitalSignThresholdId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static VitalSignThresholdId generate() {
        return new VitalSignThresholdId(UUID.randomUUID());
    }
}
