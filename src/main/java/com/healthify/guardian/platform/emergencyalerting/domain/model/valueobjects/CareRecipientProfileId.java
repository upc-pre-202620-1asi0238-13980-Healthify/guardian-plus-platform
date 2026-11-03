package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable reference to the Fragile Citizen an alert, setting or emergency contact belongs to.
 *
 * <p>Deliberately opaque: it only carries the identifier assigned by the {@code Profile}
 * bounded context. Emergency &amp; Alerting never loads the Profile domain model directly; when
 * it needs something from it, it goes through {@code ProfileContextAcl}.</p>
 *
 * @param value the referenced care recipient profile identifier
 */
public record CareRecipientProfileId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "care-recipient-profile.id.invalid";

    public CareRecipientProfileId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
