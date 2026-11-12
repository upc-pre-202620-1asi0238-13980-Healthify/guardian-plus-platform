package com.healthify.guardian.platform.emergencyalerting.application.commandservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ActivateSilentModeCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DeactivateSilentModeCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.UpdateAlertSettingsCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code AlertSettings} aggregate. Settings are
 * created with their defaults the first time a Fragile Citizen's configuration is changed.
 */
public interface AlertSettingsCommandService {

    Result<AlertSettings, ApplicationError> handle(UpdateAlertSettingsCommand command);

    Result<AlertSettings, ApplicationError> handle(ActivateSilentModeCommand command);

    Result<AlertSettings, ApplicationError> handle(DeactivateSilentModeCommand command);
}
