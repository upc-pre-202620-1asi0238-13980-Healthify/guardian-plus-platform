package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.UserPreferencesPersistenceEntity;

/**
 * Static assembler between the {@link UserPreferences} aggregate
 * and its persistence entity.
 */
public final class UserPreferencesPersistenceAssembler {

    private UserPreferencesPersistenceAssembler() {
    }

    public static UserPreferences toDomainFromPersistence(
            UserPreferencesPersistenceEntity entity) {

        if (entity == null) {
            return null;
        }

        var preferences = new UserPreferences();

        preferences.setUserId(new UserId(entity.getId()));
        preferences.setLanguage(entity.getLanguage());
        preferences.setNotificationsEnabled(
                entity.isNotificationsEnabled());
        preferences.setHighContrastEnabled(
                entity.isHighContrastEnabled());
        preferences.setReduceMotionEnabled(
                entity.isReduceMotionEnabled());
        preferences.setFontScale(entity.getFontScale());

        if (entity.getUpdatedAt() != null) {
            preferences.setUpdatedAt(
                    entity.getUpdatedAt().toInstant());
        }

        return preferences;
    }

    public static UserPreferencesPersistenceEntity toPersistenceFromDomain(
            UserPreferences preferences) {

        if (preferences == null) {
            return null;
        }

        var entity = new UserPreferencesPersistenceEntity();

        entity.setId(preferences.getUserId().value());
        entity.setLanguage(preferences.getLanguage());
        entity.setNotificationsEnabled(
                preferences.isNotificationsEnabled());
        entity.setHighContrastEnabled(
                preferences.isHighContrastEnabled());
        entity.setReduceMotionEnabled(
                preferences.isReduceMotionEnabled());
        entity.setFontScale(preferences.getFontScale());

        return entity;
    }
}