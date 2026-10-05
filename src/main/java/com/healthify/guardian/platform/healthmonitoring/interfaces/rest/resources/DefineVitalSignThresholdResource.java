package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload defining (or redefining) the clinical range of a vital sign type for a care recipient.
 */
public record DefineVitalSignThresholdResource(
        @NotNull(message = "{care-recipient-profile.id.invalid}")
        UUID careRecipientProfileId,

        @NotNull(message = "{vital-sign-type.id.invalid}")
        UUID vitalSignTypeId,

        @NotNull(message = "{vital-sign-threshold.range.invalid}")
        @Schema(example = "60")
        BigDecimal minimumValue,

        @NotNull(message = "{vital-sign-threshold.range.invalid}")
        @Schema(example = "100")
        BigDecimal maximumValue,

        @NotNull(message = "{vital-sign-threshold.consecutive-hits.invalid}")
        @Min(value = 1, message = "{vital-sign-threshold.consecutive-hits.invalid}")
        @Schema(description = "Consecutive out-of-range readings that confirm an anomaly", example = "3")
        Integer requiredConsecutiveHits
) {
}
