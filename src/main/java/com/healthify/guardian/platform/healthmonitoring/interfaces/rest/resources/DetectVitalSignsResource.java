package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Request payload of a single vital sign reading sent by a wearable device. The reception time is assigned by the server.
 */
public record DetectVitalSignsResource(
        @NotNull(message = "{wearable-device.id.invalid}")
        @Schema(description = "Wearable device that took the reading")
        UUID wearableDeviceId,

        @NotNull(message = "{care-recipient-profile.id.invalid}")
        @Schema(description = "Monitored care recipient")
        UUID careRecipientProfileId,

        @NotNull(message = "{vital-sign-type.id.invalid}")
        @Schema(description = "Vital sign type from the catalog (GET /api/v1/vital-sign-types)")
        UUID vitalSignTypeId,

        @NotNull(message = "{vital-sign.value.invalid}")
        @Schema(description = "Measured value, in the unit of its type", example = "72")
        BigDecimal value,

        @NotNull(message = "{vital-sign.measured-at.invalid}")
        @Schema(description = "When the wearable took the reading (ISO-8601)", example = "2026-10-05T14:30:00Z")
        Instant measuredAt
) {
}
