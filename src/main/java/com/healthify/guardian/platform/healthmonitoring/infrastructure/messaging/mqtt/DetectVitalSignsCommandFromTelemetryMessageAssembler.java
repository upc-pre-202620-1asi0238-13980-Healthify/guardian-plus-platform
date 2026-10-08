package com.healthify.guardian.platform.healthmonitoring.infrastructure.messaging.mqtt;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;

import java.time.Instant;
import java.util.UUID;

/**
 * Assembler converting a {@link VitalSignTelemetryMessage} received from the broker into a {@link DetectVitalSignsCommand}.
 * The reception time is the server time at which the message arrived.
 */
public final class DetectVitalSignsCommandFromTelemetryMessageAssembler {

    private DetectVitalSignsCommandFromTelemetryMessageAssembler() {
    }

    /**
     * @throws IllegalArgumentException when a mandatory field is missing or an identifier is not a UUID
     */
    public static DetectVitalSignsCommand toCommandFromMessage(VitalSignTelemetryMessage message, Instant receivedAt) {
        if (message.vitalSignTypeCode() == null || message.value() == null || message.measuredAt() == null) {
            throw new IllegalArgumentException("vital sign reading without type, value or measuredAt");
        }
        return new DetectVitalSignsCommand(
                toUuid(message.deviceId(), "deviceId"),
                toUuid(message.careRecipientProfileId(), "careRecipientProfileId"),
                message.vitalSignTypeCode(),
                message.value(),
                message.measuredAt().toInstant(),
                receivedAt);
    }

    private static UUID toUuid(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is missing");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " is not a UUID: " + value, e);
        }
    }
}
