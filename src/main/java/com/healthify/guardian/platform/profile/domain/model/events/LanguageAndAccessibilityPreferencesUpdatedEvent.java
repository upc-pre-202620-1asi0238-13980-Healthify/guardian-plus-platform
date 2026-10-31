package com.healthify.guardian.platform.profile.domain.model.events;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.FontScale;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.Language;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

import java.time.Instant;

/**
 * Raised when language or accessibility preferences are updated.
 */
public record LanguageAndAccessibilityPreferencesUpdatedEvent(
        UserId userId,
        Language language,
        boolean highContrastEnabled,
        boolean reduceMotionEnabled,
        FontScale fontScale,
        Instant updatedAt) {

    public static LanguageAndAccessibilityPreferencesUpdatedEvent from(
            UserPreferences preferences) {

        return new LanguageAndAccessibilityPreferencesUpdatedEvent(
                preferences.getUserId(),
                preferences.getLanguage(),
                preferences.isHighContrastEnabled(),
                preferences.isReduceMotionEnabled(),
                preferences.getFontScale(),
                preferences.getUpdatedAt());
    }
}