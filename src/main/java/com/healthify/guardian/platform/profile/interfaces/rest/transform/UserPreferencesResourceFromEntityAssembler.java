package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UserPreferencesResource;

/**
 * Assembler that converts a {@link UserPreferences}
 * aggregate into a {@link UserPreferencesResource}.
 */
public final class UserPreferencesResourceFromEntityAssembler {

    private UserPreferencesResourceFromEntityAssembler() {
    }

    public static UserPreferencesResource toResourceFromEntity(
            UserPreferences preferences) {

        return new UserPreferencesResource(
                preferences.getUserId().value(),
                preferences.getLanguage().name(),
                preferences.isNotificationsEnabled(),
                preferences.isHighContrastEnabled(),
                preferences.isReduceMotionEnabled(),
                preferences.getFontScale().name(),
                preferences.getUpdatedAt());
    }
}