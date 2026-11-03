package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.HealthReportResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignSummaryResource;

/**
 * Assembler converting a {@link HealthReport} aggregate into a {@link HealthReportResource}.
 */
public final class HealthReportResourceFromEntityAssembler {

    private HealthReportResourceFromEntityAssembler() {
    }

    public static HealthReportResource toResourceFromEntity(HealthReport report) {
        return new HealthReportResource(
                report.getId().value(),
                report.getCareRecipientProfileId().value(),
                report.getGeneratedByUserId() == null ? null : report.getGeneratedByUserId().value(),
                report.getReportType().name(),
                report.getPeriod().startDate(),
                report.getPeriod().endDate(),
                report.getSummaries().stream()
                        .map(summary -> new VitalSignSummaryResource(summary.getId(), summary.getMetricType(),
                                summary.getAverageValue(), summary.getMinValue(), summary.getMaxValue(),
                                summary.getReadingsCount(), summary.getOutOfRangeCount(), summary.getStabilityIndex()))
                        .toList(),
                report.getRecurrentAnomaliesCount(),
                report.isClinicallyStable(),
                report.getGeneratedAt());
    }
}
