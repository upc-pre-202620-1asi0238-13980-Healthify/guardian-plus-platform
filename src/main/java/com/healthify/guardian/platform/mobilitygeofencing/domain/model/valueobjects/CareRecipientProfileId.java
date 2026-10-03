package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.util.UUID;

public record CareRecipientProfileId(UUID value) {
    public CareRecipientProfileId {
        if (value == null) {
            throw new IllegalArgumentException("CareRecipientProfileId no puede ser nulo.");
        }
    }
}