package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ConfigureAlertChannelCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.ConfigureAlertChannelResource;

import java.util.UUID;

/**
 * Assembler to convert a {@link ConfigureAlertChannelResource} to a {@link ConfigureAlertChannelCommand}.
 */
public final class ConfigureAlertChannelCommandFromResourceAssembler {

    private ConfigureAlertChannelCommandFromResourceAssembler() {
    }

    public static ConfigureAlertChannelCommand toCommandFromResource(
            UUID userId, NotificationChannel channel, ConfigureAlertChannelResource resource) {
        return new ConfigureAlertChannelCommand(userId, channel, resource.enabled(), resource.deviceToken());
    }
}
