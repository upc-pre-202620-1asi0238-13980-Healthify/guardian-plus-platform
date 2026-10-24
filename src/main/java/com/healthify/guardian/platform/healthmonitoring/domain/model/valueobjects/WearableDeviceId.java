package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier of a {@code WearableDevice} aggregate.
 *
 * @param value the underlying UUID
 */
public record WearableDeviceId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "wearable-device.id.invalid";

    public WearableDeviceId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static WearableDeviceId generate() {
        return new WearableDeviceId(UUID.randomUUID());
    }
}
