package com.healthify.guardian.platform.healthmonitoring.application.queryservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetAllHealthReportsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetHealthReportByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for health report read queries.
 */
public interface HealthReportQueryService {

    Optional<HealthReport> handle(GetHealthReportByIdQuery query);

    /**
     * Retrieves every report of a care recipient, newest first.
     */
    List<HealthReport> handle(GetAllHealthReportsByCareRecipientProfileIdQuery query);
}
