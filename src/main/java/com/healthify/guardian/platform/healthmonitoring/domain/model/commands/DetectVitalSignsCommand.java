package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Command to register the detection of a single vital sign reading sent by a wearable device.
 *
 * @param vitalSignType code of the {@code VitalSignType}, e.g. {@code HR}
 */
public record DetectVitalSignsCommand(
        UUID wearableDeviceId,
        UUID careRecipientProfileId,
        String vitalSignType,
        BigDecimal value,
        Instant measuredAt,
        Instant receivedAt) {
}
