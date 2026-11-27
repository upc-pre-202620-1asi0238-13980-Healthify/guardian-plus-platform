package com.healthify.guardian.platform.emergencyalerting.application.internal.queryservices;

import com.healthify.guardian.platform.emergencyalerting.application.queryservices.AlertChannelSettingQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertChannelSettingsByUserIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertChannelSettingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that answers alert channel setting queries.
 */
@Service
public class AlertChannelSettingQueryServiceImpl implements AlertChannelSettingQueryService {

    private final AlertChannelSettingRepository alertChannelSettingRepository;

    public AlertChannelSettingQueryServiceImpl(AlertChannelSettingRepository alertChannelSettingRepository) {
        this.alertChannelSettingRepository = alertChannelSettingRepository;
    }

    @Override
    public List<AlertChannelSetting> handle(GetAlertChannelSettingsByUserIdQuery query) {
        return alertChannelSettingRepository.findByUserId(query.userId());
    }
}
