package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertChannelSettingId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;


/**
 * Raised when a Care Circle member enables or disables a notification channel.
 */
public record AlertChannelSettingChangedEvent(
        AlertChannelSettingId alertChannelSettingId,
        UserId userId,
        NotificationChannel channel,
        boolean enabled) {

    public static AlertChannelSettingChangedEvent from(AlertChannelSetting setting) {
        return new AlertChannelSettingChangedEvent(
                setting.getId(), setting.getUserId(), setting.getChannel(), setting.isEnabled());
    }
}
