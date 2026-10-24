package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable reference to the monitored care recipient, owned by the {@code Profile} bounded context.
 *
 * @param value the underlying UUID
 */
public record CareRecipientProfileId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "care-recipient-profile.id.invalid";

    public CareRecipientProfileId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
