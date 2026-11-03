package com.healthify.guardian.platform.profile.domain.model.events;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

import java.time.Instant;

/**
 * Raised when the general application preferences of a user are updated.
 */
public record ApplicationPreferencesUpdatedEvent(
        UserId userId,
        boolean notificationsEnabled,
        Instant updatedAt) {

    public static ApplicationPreferencesUpdatedEvent from(
            UserPreferences preferences) {

        return new ApplicationPreferencesUpdatedEvent(
                preferences.getUserId(),
                preferences.isNotificationsEnabled(),
                preferences.getUpdatedAt());
    }
}