package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Latest emitted reading of one vital sign type, classified against the threshold in force (US01-US05).
 */
public record LiveVitalSignResource(
        UUID vitalSignId,
        UUID vitalSignTypeId,
        String vitalSignTypeCode,
        String vitalSignTypeName,
        String unit,
        BigDecimal value,
        Instant measuredAt,
        @Schema(description = "BELOW_RANGE, WITHIN_RANGE or ABOVE_RANGE; null when no active threshold is defined")
        String classification,
        @Schema(description = "False when the latest reading is older than the live signal window: the last known value is shown without live signal")
        boolean liveSignal
) {
}
