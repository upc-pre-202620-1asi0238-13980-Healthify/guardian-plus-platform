package com.healthify.guardian.platform.profile.domain.model.aggregates;

import com.healthify.guardian.platform.profile.domain.model.commands.CreateCareRecipientProfileCommand;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CareRecipientProfileTest {

    @Test
    void shouldCreateCareRecipientProfileWithValidData() {

        // Arrange
        var userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111");

        var birthDate = LocalDate.of(
                1950,
                5,
                10);

        var createdAt = Instant.parse(
                "2026-10-03T20:00:00Z");

        var command =
                new CreateCareRecipientProfileCommand(
                        userId,
                        "Maria",
                        "Sanchez",
                        birthDate,
                        "https://example.com/maria.jpg");

        // Act
        var profile =
                new CareRecipientProfile(
                        command,
                        createdAt);

        // Assert
        assertNotNull(profile.getId());

        assertEquals(
                userId,
                profile.getCreatedByUserId().value());

        assertEquals(
                "Maria",
                profile.getFirstName());

        assertEquals(
                "Sanchez",
                profile.getLastName());

        assertEquals(
                birthDate,
                profile.getBirthDate());

        assertEquals(
                "https://example.com/maria.jpg",
                profile.getProfileImageUrl());

        assertEquals(
                createdAt,
                profile.getCreatedAt());
    }

    @Test
    void shouldUpdatePersonalInformation() {

        // Arrange
        var command =
                new CreateCareRecipientProfileCommand(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111"),
                        "Maria",
                        "Sanchez",
                        LocalDate.of(
                                1950,
                                5,
                                10),
                        null);

        var profile =
                new CareRecipientProfile(
                        command,
                        Instant.parse(
                                "2026-10-03T20:00:00Z"));

        var newBirthDate =
                LocalDate.of(
                        1951,
                        6,
                        15);

        var updatedAt = Instant.parse(
                "2026-10-03T21:00:00Z");

        // Act
        profile.updatePersonalInformation(
                "Maria Elena",
                "Sanchez Torres",
                newBirthDate,
                updatedAt);

        // Assert
        assertEquals(
                "Maria Elena",
                profile.getFirstName());

        assertEquals(
                "Sanchez Torres",
                profile.getLastName());

        assertEquals(
                newBirthDate,
                profile.getBirthDate());

        assertEquals(
                updatedAt,
                profile.getUpdatedAt());
    }

    @Test
    void shouldUpdateProfileImage() {

        // Arrange
        var command =
                new CreateCareRecipientProfileCommand(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111"),
                        "Maria",
                        "Sanchez",
                        LocalDate.of(
                                1950,
                                5,
                                10),
                        null);

        var profile =
                new CareRecipientProfile(
                        command,
                        Instant.parse(
                                "2026-10-03T20:00:00Z"));

        var updatedAt = Instant.parse(
                "2026-10-03T21:00:00Z");

        // Act
        profile.updateProfileImage(
                "https://example.com/new-image.jpg",
                updatedAt);

        // Assert
        assertEquals(
                "https://example.com/new-image.jpg",
                profile.getProfileImageUrl());

        assertEquals(
                updatedAt,
                profile.getUpdatedAt());
    }

    @Test
    void shouldRejectNullBirthDate() {

        // Arrange
        var command =
                new CreateCareRecipientProfileCommand(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111"),
                        "Maria",
                        "Sanchez",
                        null,
                        null);

        var createdAt = Instant.parse(
                "2026-10-03T20:00:00Z");

        // Act
        var exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new CareRecipientProfile(
                                command,
                                createdAt));

        // Assert
        assertEquals(
                "care-recipient-profile.birth-date.invalid",
                exception.getMessage());
    }
}