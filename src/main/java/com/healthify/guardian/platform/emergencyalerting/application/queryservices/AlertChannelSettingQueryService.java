package com.healthify.guardian.platform.emergencyalerting.application.queryservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertChannelSettingsByUserIdQuery;

import java.util.List;

/**
 * Application service contract for queries over the {@code AlertChannelSetting} aggregate.
 */
public interface AlertChannelSettingQueryService {

    List<AlertChannelSetting> handle(GetAlertChannelSettingsByUserIdQuery query);
}
