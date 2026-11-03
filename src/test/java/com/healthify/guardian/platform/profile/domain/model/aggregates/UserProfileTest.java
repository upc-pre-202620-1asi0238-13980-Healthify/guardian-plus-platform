package com.healthify.guardian.platform.profile.domain.model.aggregates;

import com.healthify.guardian.platform.profile.domain.model.commands.CreateUserProfileCommand;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserProfileTest {

    @Test
    void shouldCreateUserProfileWithValidData() {

        // Arrange
        var userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111");

        var createdAt = Instant.parse(
                "2026-10-03T20:00:00Z");

        var command = new CreateUserProfileCommand(
                userId,
                "Juan",
                "Sanchez",
                "999999999",
                "https://example.com/profile.jpg");

        // Act
        var profile = new UserProfile(
                command,
                createdAt);

        // Assert
        assertNotNull(profile.getId());
        assertEquals(
                userId,
                profile.getUserId().value());

        assertEquals(
                "Juan",
                profile.getFirstName());

        assertEquals(
                "Sanchez",
                profile.getLastName());

        assertEquals(
                "999999999",
                profile.getPhoneNumber());

        assertEquals(
                "https://example.com/profile.jpg",
                profile.getProfileImageUrl());

        assertEquals(
                createdAt,
                profile.getCreatedAt());

        assertEquals(
                createdAt,
                profile.getUpdatedAt());
    }

    @Test
    void shouldUpdatePersonalInformation() {

        // Arrange
        var command = new CreateUserProfileCommand(
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111111"),
                "Juan",
                "Sanchez",
                "999999999",
                null);

        var profile = new UserProfile(
                command,
                Instant.parse(
                        "2026-10-03T20:00:00Z"));

        var updatedAt = Instant.parse(
                "2026-10-03T21:00:00Z");

        // Act
        profile.updatePersonalInformation(
                "Juan Antonio",
                "Sanchez Cuadrado",
                updatedAt);

        // Assert
        assertEquals(
                "Juan Antonio",
                profile.getFirstName());

        assertEquals(
                "Sanchez Cuadrado",
                profile.getLastName());

        assertEquals(
                updatedAt,
                profile.getUpdatedAt());
    }

    @Test
    void shouldUpdateContactInformation() {

        // Arrange
        var command = new CreateUserProfileCommand(
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111111"),
                "Juan",
                "Sanchez",
                "999999999",
                null);

        var profile = new UserProfile(
                command,
                Instant.parse(
                        "2026-10-03T20:00:00Z"));

        var updatedAt = Instant.parse(
                "2026-10-03T21:00:00Z");

        // Act
        profile.updateContactInformation(
                "988888888",
                updatedAt);

        // Assert
        assertEquals(
                "988888888",
                profile.getPhoneNumber());

        assertEquals(
                updatedAt,
                profile.getUpdatedAt());
    }

    @Test
    void shouldRejectBlankPersonalInformation() {

        // Arrange
        var command = new CreateUserProfileCommand(
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111111"),
                "",
                "Sanchez",
                "999999999",
                null);

        var createdAt = Instant.parse(
                "2026-10-03T20:00:00Z");

        // Act
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UserProfile(
                        command,
                        createdAt));

        // Assert
        assertEquals(
                "user-profile.personal-information.invalid",
                exception.getMessage());
    }
}