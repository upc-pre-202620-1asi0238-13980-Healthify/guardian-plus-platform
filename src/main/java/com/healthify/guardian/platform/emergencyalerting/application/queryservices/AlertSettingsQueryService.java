package com.healthify.guardian.platform.emergencyalerting.application.queryservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertSettingsByCareRecipientProfileIdQuery;

/**
 * Application service contract for queries over the {@code AlertSettings} aggregate.
 */
public interface AlertSettingsQueryService {

    /**
     * @return the Fragile Citizen's settings, or the default ones if they were never changed
     */
    AlertSettings handle(GetAlertSettingsByCareRecipientProfileIdQuery query);
}
