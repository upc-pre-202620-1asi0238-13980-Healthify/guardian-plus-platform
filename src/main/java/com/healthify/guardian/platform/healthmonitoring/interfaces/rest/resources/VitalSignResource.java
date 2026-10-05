package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response payload of a stored vital sign reading.
 */
public record VitalSignResource(
        UUID id,
        UUID wearableDeviceId,
        UUID careRecipientProfileId,
        UUID vitalSignTypeId,
        BigDecimal value,
        Instant measuredAt,
        Instant receivedAt,
        Instant emittedAt
) {
}
