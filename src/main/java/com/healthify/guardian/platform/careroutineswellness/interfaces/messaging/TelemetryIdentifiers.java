package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import java.util.UUID;

/**
 * Translates the identifiers carried by the wearable's telemetry into this bounded context's own.
 */
final class TelemetryIdentifiers {

    private TelemetryIdentifiers() {
    }

    /**
     * The wearable references the patient by its Profile {@code careRecipientProfileId}, which is exactly
     * what this context calls the person under care.
     *
     * @throws IllegalArgumentException when the identifier is missing or not a UUID
     */
    static UUID toPersonUnderCareId(String careRecipientProfileId) {
        if (careRecipientProfileId == null) {
            throw new IllegalArgumentException("careRecipientProfileId is missing");
        }
        try {
            return UUID.fromString(careRecipientProfileId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("careRecipientProfileId is not a UUID: " + careRecipientProfileId, e);
        }
    }
}
