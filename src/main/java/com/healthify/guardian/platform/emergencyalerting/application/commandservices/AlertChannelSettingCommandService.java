package com.healthify.guardian.platform.emergencyalerting.application.commandservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ConfigureAlertChannelCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code AlertChannelSetting} aggregate.
 */
public interface AlertChannelSettingCommandService {

    /**
     * Enables or disables a notification channel for a Care Circle member, optionally registering
     * the push device token. Rejects disabling the member's last enabled channel.
     *
     * @param command the channel configuration
     * @return the channel setting or an application error
     */
    Result<AlertChannelSetting, ApplicationError> handle(ConfigureAlertChannelCommand command);
}
