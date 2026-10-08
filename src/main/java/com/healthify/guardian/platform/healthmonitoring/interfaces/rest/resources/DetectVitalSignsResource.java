package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

        @NotBlank(message = "{vital-sign.type.invalid}")
        @Schema(description = "Vital sign type code (GET /api/v1/vital-sign-types)",
                allowableValues = {"HR", "BP_SYS", "BP_DIA", "SPO2", "TEMP", "RESP_RATE"}, example = "HR")
        String vitalSignType,

        @NotNull(message = "{vital-sign.value.invalid}")
        @Schema(description = "Measured value, in the unit of its type", example = "72")
        BigDecimal value,

        @NotNull(message = "{vital-sign.measured-at.invalid}")
        @Schema(description = "When the wearable took the reading (ISO-8601)", example = "2026-10-05T14:30:00Z")
        Instant measuredAt
) {
}
