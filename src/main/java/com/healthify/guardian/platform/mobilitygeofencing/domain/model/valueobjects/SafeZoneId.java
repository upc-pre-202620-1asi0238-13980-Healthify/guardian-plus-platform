package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.util.UUID;

public record SafeZoneId(UUID value) {
    public SafeZoneId {
        if (value == null) {
            throw new IllegalArgumentException("SafeZoneId is not null.");
        }
    }

    public static SafeZoneId generate() {
        return new SafeZoneId(UUID.randomUUID());
    }
}