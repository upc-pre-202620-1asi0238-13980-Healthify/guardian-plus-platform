package com.healthify.guardian.platform.emergencyalerting.application.internal.commandservices;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertSettingsCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ActivateSilentModeCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DeactivateSilentModeCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.UpdateAlertSettingsCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AckTimeout;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertSettingsRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Application service that executes alert settings commands.
 */
@Service
public class AlertSettingsCommandServiceImpl implements AlertSettingsCommandService {

    private final AlertSettingsRepository alertSettingsRepository;

    public AlertSettingsCommandServiceImpl(AlertSettingsRepository alertSettingsRepository) {
        this.alertSettingsRepository = alertSettingsRepository;
    }

    @Override
    public Result<AlertSettings, ApplicationError> handle(UpdateAlertSettingsCommand command) {
        return applyToSettings(command.careRecipientProfileId(), "update-alert-settings", settings -> settings.update(
                new AckTimeout(command.primaryAckTimeoutSec()),
                command.escalationEnabled(),
                command.broadcastCriticalImmediately()));
    }

    @Override
    public Result<AlertSettings, ApplicationError> handle(ActivateSilentModeCommand command) {
        return applyToSettings(command.careRecipientProfileId(), "activate-silent-mode",
                AlertSettings::activateSilentMode);
    }

    @Override
    public Result<AlertSettings, ApplicationError> handle(DeactivateSilentModeCommand command) {
        return applyToSettings(command.careRecipientProfileId(), "deactivate-silent-mode",
                AlertSettings::deactivateSilentMode);
    }

    private Result<AlertSettings, ApplicationError> applyToSettings(
            UUID careRecipientProfileId, String operation, Consumer<AlertSettings> change) {
        try {
            var profileId = new CareRecipientProfileId(careRecipientProfileId);
            var settings = alertSettingsRepository.findByCareRecipientProfileId(profileId)
                    .orElseGet(() -> new AlertSettings(profileId));
            change.accept(settings);
            return Result.success(alertSettingsRepository.save(settings));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(operation, resolve(e)));
        }
    }

    /** Resolves a domain exception whose message is a bundle key into the localized sentence. */
    private static String resolve(RuntimeException e) {
        return MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage());
    }
}
