package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * Request payload to enable or disable a notification channel.
 *
 * @param enabled     whether the channel is enabled
 * @param deviceToken push device token; only for the {@code PUSH} channel
 */
public record ConfigureAlertChannelResource(
        @NotNull(message = "{alert-channel-setting.enabled.invalid}")
        Boolean enabled,

        @Size(max = 512, message = "{alert-channel-setting.device-token.invalid}")
        String deviceToken
) {
}
