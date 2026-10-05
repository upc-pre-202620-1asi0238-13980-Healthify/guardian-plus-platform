package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response payload of a vital sign threshold.
 */
public record VitalSignThresholdResource(
        UUID id,
        UUID careRecipientProfileId,
        UUID vitalSignTypeId,
        BigDecimal minimumValue,
        BigDecimal maximumValue,
        Integer requiredConsecutiveHits,
        Boolean active,
        Instant updatedAt
) {
}
