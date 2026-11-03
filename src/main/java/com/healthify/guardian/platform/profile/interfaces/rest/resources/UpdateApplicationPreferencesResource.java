package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload to update general application preferences.
 *
 * @param notificationsEnabled whether application notifications are enabled
 */
public record UpdateApplicationPreferencesResource(

        @NotNull(message = "{user-preferences.notifications-enabled.blank}")
        Boolean notificationsEnabled
) {
}