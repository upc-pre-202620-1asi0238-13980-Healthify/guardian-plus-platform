package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Latest emitted reading of one vital sign type, classified against the normal range of its type (US01-US05).
 */
public record LiveVitalSignResource(
        UUID vitalSignId,
        @Schema(example = "HR")
        String vitalSignType,
        @Schema(example = "Heart rate")
        String vitalSignTypeName,
        @Schema(example = "bpm")
        String unit,
        BigDecimal value,
        Instant measuredAt,
        @Schema(description = "Lower bound of the normal range of the type", example = "60")
        BigDecimal normalMinimum,
        @Schema(description = "Upper bound of the normal range of the type", example = "100")
        BigDecimal normalMaximum,
        @Schema(description = "BELOW_RANGE, WITHIN_RANGE or ABOVE_RANGE")
        String classification,
        @Schema(description = "False when the latest reading is older than the live signal window: the last known value is shown without live signal")
        boolean liveSignal
) {
}
