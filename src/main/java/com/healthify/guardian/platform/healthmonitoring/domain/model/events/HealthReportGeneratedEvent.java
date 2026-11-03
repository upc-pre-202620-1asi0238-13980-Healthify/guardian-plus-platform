package com.healthify.guardian.platform.healthmonitoring.domain.model.events;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportType;

import java.time.Instant;

/**
 * Raised after a health report has been compiled.
 */
public record HealthReportGeneratedEvent(
        HealthReportId healthReportId,
        CareRecipientProfileId careRecipientProfileId,
        HealthReportType reportType,
        Instant generatedAt) {
}
