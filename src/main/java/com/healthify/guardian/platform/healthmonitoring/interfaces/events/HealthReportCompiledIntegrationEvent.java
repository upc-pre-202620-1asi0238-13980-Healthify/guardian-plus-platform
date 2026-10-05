package com.healthify.guardian.platform.healthmonitoring.interfaces.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Integration event notifying supporting contexts that a new structured health report is available.
 *
 * @param healthReportId          the compiled report
 * @param careRecipientProfileId  the care recipient it describes
 * @param periodStart             first day covered
 * @param periodEnd               last day covered
 * @param recurrentAnomaliesCount vital sign types flagged as recurrent
 * @param compiledAt              when it was compiled
 */
public record HealthReportCompiledIntegrationEvent(
        UUID healthReportId,
        UUID careRecipientProfileId,
        LocalDate periodStart,
        LocalDate periodEnd,
        int recurrentAnomaliesCount,
        Instant compiledAt) {
}
