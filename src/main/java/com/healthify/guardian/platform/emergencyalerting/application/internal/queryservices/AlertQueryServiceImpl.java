package com.healthify.guardian.platform.emergencyalerting.application.internal.queryservices;

import com.healthify.guardian.platform.emergencyalerting.application.queryservices.AlertQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetActiveAlertsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertByIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertHistoryByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetPendingAlertsByRecipientUserIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that answers alert queries.
 */
@Service
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;

    public AlertQueryServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public Optional<Alert> handle(GetAlertByIdQuery query) {
        return alertRepository.findById(query.alertId());
    }

    @Override
    public List<Alert> handle(GetActiveAlertsByCareRecipientProfileIdQuery query) {
        return alertRepository.findActiveByCareRecipientProfileId(query.careRecipientProfileId());
    }

    @Override
    public Page<Alert> handle(GetAlertHistoryByCareRecipientProfileIdQuery query) {
        return alertRepository.findHistory(
                query.careRecipientProfileId(),
                query.dateRange(),
                query.severityFilter(),
                PageRequest.of(query.page(), query.size()));
    }

    @Override
    public List<Alert> handle(GetPendingAlertsByRecipientUserIdQuery query) {
        return alertRepository.findPendingByRecipientUserId(query.recipientUserId());
    }
}
