package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request payload for an on-demand health report.
 */
public record GenerateHealthReportResource(
        @NotNull(message = "{care-recipient-profile.id.invalid}")
        UUID careRecipientProfileId,

        @Schema(description = "Authenticated user requesting the report")
        UUID generatedByUserId,

        @NotNull(message = "{date-range.invalid}")
        @Schema(example = "2026-09-28")
        LocalDate periodStart,

        @NotNull(message = "{date-range.invalid}")
        @Schema(example = "2026-10-04")
        LocalDate periodEnd
) {
}
