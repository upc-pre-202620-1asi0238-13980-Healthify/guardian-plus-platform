package com.healthify.guardian.platform.profile.domain.model.aggregates;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.FontScale;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.Language;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPreferencesTest {

    @Test
    void shouldCreateUserPreferencesWithDefaultValues() {

        // Arrange
        var userId = new UserId(
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111111"));

        var updatedAt = Instant.parse(
                "2026-10-03T20:00:00Z");

        // Act
        var preferences = new UserPreferences(
                userId,
                true,
                updatedAt);

        // Assert
        assertEquals(
                userId,
                preferences.getUserId());

        assertEquals(
                Language.SPANISH_LATIN_AMERICA,
                preferences.getLanguage());

        assertTrue(
                preferences.isNotificationsEnabled());

        assertFalse(
                preferences.isHighContrastEnabled());

        assertFalse(
                preferences.isReduceMotionEnabled());

        assertEquals(
                FontScale.DEFAULT,
                preferences.getFontScale());

        assertEquals(
                updatedAt,
                preferences.getUpdatedAt());
    }

    @Test
    void shouldUpdateApplicationPreferences() {

        // Arrange
        var preferences = new UserPreferences(
                new UserId(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111")),
                true,
                Instant.parse(
                        "2026-10-03T20:00:00Z"));

        var updatedAt = Instant.parse(
                "2026-10-03T21:00:00Z");

        // Act
        preferences.updateApplicationPreferences(
                false,
                updatedAt);

        // Assert
        assertFalse(
                preferences.isNotificationsEnabled());

        assertEquals(
                updatedAt,
                preferences.getUpdatedAt());
    }

    @Test
    void shouldUpdateLanguageAndAccessibilityPreferences() {

        // Arrange
        var preferences = new UserPreferences(
                new UserId(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111")),
                true,
                Instant.parse(
                        "2026-10-03T20:00:00Z"));

        var updatedAt = Instant.parse(
                "2026-10-03T21:00:00Z");

        // Act
        preferences.updateLanguageAndAccessibilityPreferences(
                Language.ENGLISH_UNITED_STATES,
                true,
                true,
                FontScale.LARGE,
                updatedAt);

        // Assert
        assertEquals(
                Language.ENGLISH_UNITED_STATES,
                preferences.getLanguage());

        assertTrue(
                preferences.isHighContrastEnabled());

        assertTrue(
                preferences.isReduceMotionEnabled());

        assertEquals(
                FontScale.LARGE,
                preferences.getFontScale());

        assertEquals(
                updatedAt,
                preferences.getUpdatedAt());
    }

    @Test
    void shouldRejectNullLanguage() {

        // Arrange
        var preferences = new UserPreferences(
                new UserId(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111")),
                true,
                Instant.parse(
                        "2026-10-03T20:00:00Z"));

        var updatedAt = Instant.parse(
                "2026-10-03T21:00:00Z");

        // Act
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> preferences
                        .updateLanguageAndAccessibilityPreferences(
                                null,
                                false,
                                false,
                                FontScale.DEFAULT,
                                updatedAt));

        // Assert
        assertEquals(
                "user-preferences.language.invalid",
                exception.getMessage());
    }
}