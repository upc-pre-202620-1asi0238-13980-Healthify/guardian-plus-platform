package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

import java.util.UUID;

/**
 * Origin of an alert: the kind of signal and the record, in the supplier context, that raised it
 * (a biometric reading, a safe zone, an activity monitor, a reminder, a medication stock or the
 * wearable device itself).
 *
 * @param sourceType        the kind of signal
 * @param sourceReferenceId the record that originated the signal
 */
public record AlertSource(AlertSourceType sourceType, UUID sourceReferenceId) {

    private static final String TYPE_INVALID_MESSAGE_KEY = "alert-source.type.invalid";
    private static final String REFERENCE_INVALID_MESSAGE_KEY = "alert-source.reference.invalid";

    public AlertSource {
        if (sourceType == null) {
            throw new IllegalArgumentException(TYPE_INVALID_MESSAGE_KEY);
        }
        if (sourceReferenceId == null) {
            throw new IllegalArgumentException(REFERENCE_INVALID_MESSAGE_KEY);
        }
    }

    /** Tells whether the Fragile Citizen gets a window to cancel this alert before it is dispatched. */
    public boolean requiresConfirmationWindow() {
        return sourceType == AlertSourceType.FALL_DETECTED;
    }
}
