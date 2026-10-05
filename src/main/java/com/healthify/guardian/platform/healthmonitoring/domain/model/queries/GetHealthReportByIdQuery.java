package com.healthify.guardian.platform.healthmonitoring.domain.model.queries;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;

/**
 * Query for a single health report.
 */
public record GetHealthReportByIdQuery(HealthReportId healthReportId) {
}
