package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable reference to the person under care that a routine or wellness fact belongs to.
 *
 * <p>Deliberately opaque: it only carries the identifier assigned by the {@code Profile}
 * bounded context (the owner of {@code CareRecipientProfileId}). Care Routines &amp; Wellness
 * never loads or depends on the Profile domain model directly, keeping the two contexts
 * decoupled; the reference is trusted as-is, consistent with how every other supporting/generic
 * bounded context in this platform treats cross-context identifiers.</p>
 *
 * @param value the referenced person-under-care identifier
 */
public record PersonUnderCareId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "person-under-care.id.invalid";

    public PersonUnderCareId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
