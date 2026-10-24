package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import java.util.UUID;

/**
 * Response payload representing a notification channel of a Care Circle member. The device
 * token itself is never exposed.
 *
 * @param userId                the Care Circle member
 * @param channel               the notification channel
 * @param enabled               whether the channel is enabled
 * @param deviceTokenRegistered whether a push device token is registered
 */
public record AlertChannelSettingResource(
        UUID userId,
        String channel,
        boolean enabled,
        boolean deviceTokenRegistered
) {
}
