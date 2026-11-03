package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to compile a health report for a care recipient over a period.
 */
public record GenerateHealthReportCommand(
        UUID careRecipientProfileId,
        UUID generatedByUserId,
        String reportType,
        LocalDate periodStart,
        LocalDate periodEnd) {
}
