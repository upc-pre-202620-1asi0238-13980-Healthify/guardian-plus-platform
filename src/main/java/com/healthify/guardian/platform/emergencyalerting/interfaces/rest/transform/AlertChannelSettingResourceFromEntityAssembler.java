package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertChannelSettingResource;

/**
 * Assembler that converts an {@link AlertChannelSetting} domain aggregate into an
 * {@link AlertChannelSettingResource}, without exposing the device token.
 */
public final class AlertChannelSettingResourceFromEntityAssembler {

    private AlertChannelSettingResourceFromEntityAssembler() {
    }

    public static AlertChannelSettingResource toResourceFromEntity(AlertChannelSetting setting) {
        return new AlertChannelSettingResource(
                setting.getUserId().value(),
                setting.getChannel().name(),
                setting.isEnabled(),
                setting.getDeviceToken() != null);
    }
}
