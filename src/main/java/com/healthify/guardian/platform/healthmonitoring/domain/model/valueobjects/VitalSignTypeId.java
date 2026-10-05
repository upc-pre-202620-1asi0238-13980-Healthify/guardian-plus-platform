package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier of a {@code VitalSignType} aggregate.
 *
 * @param value the underlying UUID
 */
public record VitalSignTypeId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "vital-sign-type.id.invalid";

    public VitalSignTypeId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static VitalSignTypeId generate() {
        return new VitalSignTypeId(UUID.randomUUID());
    }
}
