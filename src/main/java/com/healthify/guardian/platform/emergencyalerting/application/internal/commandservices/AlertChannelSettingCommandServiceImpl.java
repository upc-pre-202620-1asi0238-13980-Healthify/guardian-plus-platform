package com.healthify.guardian.platform.emergencyalerting.application.internal.commandservices;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertChannelSettingCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ConfigureAlertChannelCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertChannelSettingRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

/**
 * Application service that executes alert channel setting commands.
 */
@Service
public class AlertChannelSettingCommandServiceImpl implements AlertChannelSettingCommandService {

    private static final String CHANNEL_INVALID_MESSAGE_KEY = "alert-channel-setting.channel.invalid";
    private static final String ENABLED_INVALID_MESSAGE_KEY = "alert-channel-setting.enabled.invalid";
    private static final String LAST_ENABLED_MESSAGE_KEY = "alert-channel-setting.last-enabled";

    private final AlertChannelSettingRepository alertChannelSettingRepository;

    public AlertChannelSettingCommandServiceImpl(AlertChannelSettingRepository alertChannelSettingRepository) {
        this.alertChannelSettingRepository = alertChannelSettingRepository;
    }

    @Override
    public Result<AlertChannelSetting, ApplicationError> handle(ConfigureAlertChannelCommand command) {
        try {
            var userId = new UserId(command.userId());
            if (command.channel() == null) {
                throw new IllegalArgumentException(CHANNEL_INVALID_MESSAGE_KEY);
            }
            if (command.enabled() == null) {
                throw new IllegalArgumentException(ENABLED_INVALID_MESSAGE_KEY);
            }
            if (!command.enabled()) {
                var keepsAnotherChannel = alertChannelSettingRepository.findByUserId(userId).stream()
                        .anyMatch(setting -> setting.isEnabled() && setting.getChannel() != command.channel());
                if (!keepsAnotherChannel) {
                    return Result.failure(ApplicationError.businessRuleViolation(
                            "configure-alert-channel", resolve(LAST_ENABLED_MESSAGE_KEY)));
                }
            }

            var existing = alertChannelSettingRepository.findByUserIdAndChannel(userId, command.channel());
            if (existing.isEmpty()) {
                return Result.success(alertChannelSettingRepository.save(new AlertChannelSetting(command)));
            }

            var setting = existing.get();
            if (command.enabled()) {
                setting.enable();
            } else {
                setting.disable();
            }
            if (command.deviceToken() != null) {
                setting.registerDeviceToken(command.deviceToken());
            }
            return Result.success(alertChannelSettingRepository.save(setting));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("configure-alert-channel", resolve(e.getMessage())));
        }
    }

    private static String resolve(String messageKey) {
        return MessageResolver.resolveOrDefault(messageKey, messageKey);
    }
}
