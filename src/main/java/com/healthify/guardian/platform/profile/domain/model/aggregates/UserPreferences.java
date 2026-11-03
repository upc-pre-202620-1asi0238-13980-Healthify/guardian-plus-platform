package com.healthify.guardian.platform.profile.domain.model.aggregates;

import com.healthify.guardian.platform.profile.domain.model.events.ApplicationPreferencesUpdatedEvent;
import com.healthify.guardian.platform.profile.domain.model.events.LanguageAndAccessibilityPreferencesUpdatedEvent;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.FontScale;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.Language;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root that stores application, language and accessibility
 * preferences for a Guardian+ user.
 */
@Getter
public class UserPreferences
        extends AbstractDomainAggregateRoot<UserPreferences> {

    private static final String INVALID_USER_MESSAGE_KEY =
            "user.id.invalid";

    private static final String INVALID_LANGUAGE_MESSAGE_KEY =
            "user-preferences.language.invalid";

    private static final String INVALID_FONT_SCALE_MESSAGE_KEY =
            "user-preferences.font-scale.invalid";

    private static final String INVALID_TIMESTAMP_MESSAGE_KEY =
            "user-preferences.timestamp.invalid";

    private UserId userId;
    private Language language;
    private boolean notificationsEnabled;
    private boolean highContrastEnabled;
    private boolean reduceMotionEnabled;
    private FontScale fontScale;
    private Instant updatedAt;

    /**
     * Empty constructor used when reconstituting the aggregate
     * from persistence.
     */
    public UserPreferences() {
    }

    /**
     * Creates the initial preference set for a user.
     *
     * The visual defaults are taken from the current Guardian+ UI:
     * Spanish (Latin America), default text size, high contrast disabled
     * and reduced motion disabled.
     *
     * The notifications value comes from the user request instead of
     * assuming a default that has not been defined by the product yet.
     */
    public UserPreferences(
            UserId userId,
            boolean notificationsEnabled,
            Instant updatedAt) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    INVALID_USER_MESSAGE_KEY);
        }

        validateTimestamp(updatedAt);

        this.userId = userId;
        this.language = Language.SPANISH_LATIN_AMERICA;
        this.notificationsEnabled = notificationsEnabled;
        this.highContrastEnabled = false;
        this.reduceMotionEnabled = false;
        this.fontScale = FontScale.DEFAULT;
        this.updatedAt = updatedAt;

        registerDomainEvent(
                ApplicationPreferencesUpdatedEvent.from(this));
    }

    /**
     * Updates general application preferences.
     */
    public void updateApplicationPreferences(
            boolean notificationsEnabled,
            Instant updatedAt) {

        validateTimestamp(updatedAt);

        this.notificationsEnabled = notificationsEnabled;
        this.updatedAt = updatedAt;

        registerDomainEvent(
                ApplicationPreferencesUpdatedEvent.from(this));
    }

    /**
     * Updates language and accessibility preferences in a single
     * domain operation.
     */
    public void updateLanguageAndAccessibilityPreferences(
            Language language,
            boolean highContrastEnabled,
            boolean reduceMotionEnabled,
            FontScale fontScale,
            Instant updatedAt) {

        if (language == null) {
            throw new IllegalArgumentException(
                    INVALID_LANGUAGE_MESSAGE_KEY);
        }

        if (fontScale == null) {
            throw new IllegalArgumentException(
                    INVALID_FONT_SCALE_MESSAGE_KEY);
        }

        validateTimestamp(updatedAt);

        this.language = language;
        this.highContrastEnabled = highContrastEnabled;
        this.reduceMotionEnabled = reduceMotionEnabled;
        this.fontScale = fontScale;
        this.updatedAt = updatedAt;

        registerDomainEvent(
                LanguageAndAccessibilityPreferencesUpdatedEvent.from(this));
    }

    private static void validateTimestamp(Instant timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException(
                    INVALID_TIMESTAMP_MESSAGE_KEY);
        }
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public void setHighContrastEnabled(boolean highContrastEnabled) {
        this.highContrastEnabled = highContrastEnabled;
    }

    public void setReduceMotionEnabled(boolean reduceMotionEnabled) {
        this.reduceMotionEnabled = reduceMotionEnabled;
    }

    public void setFontScale(FontScale fontScale) {
        this.fontScale = fontScale;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}