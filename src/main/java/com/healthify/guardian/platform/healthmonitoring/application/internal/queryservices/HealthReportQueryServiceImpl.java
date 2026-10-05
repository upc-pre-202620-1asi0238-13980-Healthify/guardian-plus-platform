package com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices;

import com.healthify.guardian.platform.healthmonitoring.application.queryservices.HealthReportQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetAllHealthReportsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetHealthReportByIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.HealthReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that resolves health report read queries.
 */
@Service
public class HealthReportQueryServiceImpl implements HealthReportQueryService {

    private final HealthReportRepository healthReportRepository;

    public HealthReportQueryServiceImpl(HealthReportRepository healthReportRepository) {
        this.healthReportRepository = healthReportRepository;
    }

    @Override
    public Optional<HealthReport> handle(GetHealthReportByIdQuery query) {
        return healthReportRepository.findById(query.healthReportId());
    }

    @Override
    public List<HealthReport> handle(GetAllHealthReportsByCareRecipientProfileIdQuery query) {
        return healthReportRepository.findByCareRecipientProfileId(query.careRecipientProfileId());
    }
}
