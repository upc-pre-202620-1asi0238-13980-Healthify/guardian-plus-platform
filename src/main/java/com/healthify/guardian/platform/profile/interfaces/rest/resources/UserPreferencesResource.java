package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing a user's application preferences.
 *
 * @param userId               the Guardian+ user identifier
 * @param language             the selected application language
 * @param notificationsEnabled whether notifications are enabled
 * @param highContrastEnabled  whether high contrast is enabled
 * @param reduceMotionEnabled  whether reduced motion is enabled
 * @param fontScale            the selected text size
 * @param updatedAt            when the preferences were last updated
 */
public record UserPreferencesResource(
        UUID userId,
        String language,
        boolean notificationsEnabled,
        boolean highContrastEnabled,
        boolean reduceMotionEnabled,
        String fontScale,
        Instant updatedAt
) {
}