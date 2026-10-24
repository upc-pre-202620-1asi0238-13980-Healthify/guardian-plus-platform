package com.healthify.guardian.platform.healthmonitoring.domain.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;

import java.util.List;
import java.util.Optional;

/**
 * Health report aggregate repository port.
 */
public interface HealthReportRepository {

    HealthReport save(HealthReport report);

    Optional<HealthReport> findById(HealthReportId id);

    /**
     * Retrieves every report of a care recipient, newest first.
     */
    List<HealthReport> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId);
}
