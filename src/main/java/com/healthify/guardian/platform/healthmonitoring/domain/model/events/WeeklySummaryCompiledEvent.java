package com.healthify.guardian.platform.healthmonitoring.domain.model.events;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;

import java.time.Instant;

/**
 * Raised by the scheduled weekly compilation once a weekly summary report exists.
 */
public record WeeklySummaryCompiledEvent(
        HealthReportId healthReportId,
        CareRecipientProfileId careRecipientProfileId,
        DateRange period,
        int recurrentAnomaliesCount,
        Instant compiledAt) {
}
