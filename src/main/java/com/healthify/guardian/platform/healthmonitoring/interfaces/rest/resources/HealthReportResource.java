package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response payload of a compiled health report.
 */
public record HealthReportResource(
        UUID id,
        UUID careRecipientProfileId,
        UUID generatedByUserId,
        String reportType,
        LocalDate periodStart,
        LocalDate periodEnd,
        List<VitalSignSummaryResource> summaries,
        Integer recurrentAnomaliesCount,
        boolean clinicallyStable,
        Instant generatedAt
) {
}
