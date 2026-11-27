package com.healthify.guardian.platform.emergencyalerting.application.internal.queryservices;

import com.healthify.guardian.platform.emergencyalerting.application.queryservices.AlertSettingsQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertSettingsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertSettingsRepository;
import org.springframework.stereotype.Service;

/**
 * Application service that answers alert settings queries.
 */
@Service
public class AlertSettingsQueryServiceImpl implements AlertSettingsQueryService {

    private final AlertSettingsRepository alertSettingsRepository;

    public AlertSettingsQueryServiceImpl(AlertSettingsRepository alertSettingsRepository) {
        this.alertSettingsRepository = alertSettingsRepository;
    }

    /** Returns the stored settings, or unsaved defaults so that reading never creates a row. */
    @Override
    public AlertSettings handle(GetAlertSettingsByCareRecipientProfileIdQuery query) {
        return alertSettingsRepository.findByCareRecipientProfileId(query.careRecipientProfileId())
                .orElseGet(() -> new AlertSettings(query.careRecipientProfileId()));
    }
}
