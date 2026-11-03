package com.healthify.guardian.platform.profile.domain.model.aggregates;

import com.healthify.guardian.platform.profile.domain.model.commands.EstablishCareRelationshipCommand;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipStatus;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.RelationshipType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CareRelationshipTest {

    @Test
    void shouldEstablishCareRelationshipWithValidData() {

        // Arrange
        var userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111");

        var careRecipientProfileId = UUID.fromString(
                "22222222-2222-2222-2222-222222222222");

        var startedAt = Instant.parse(
                "2026-10-03T20:00:00Z");

        var command =
                new EstablishCareRelationshipCommand(
                        userId,
                        careRecipientProfileId,
                        RelationshipType.FAMILY);

        // Act
        var relationship =
                new CareRelationship(
                        command,
                        startedAt);

        // Assert
        assertNotNull(relationship.getId());

        assertEquals(
                userId,
                relationship.getUserId().value());

        assertEquals(
                careRecipientProfileId,
                relationship.getCareRecipientProfileId().value());

        assertEquals(
                RelationshipType.FAMILY,
                relationship.getRelationshipType());

        assertEquals(
                CareRelationshipStatus.ACTIVE,
                relationship.getStatus());

        assertEquals(
                startedAt,
                relationship.getStartedAt());

        assertNull(
                relationship.getEndedAt());

        assertTrue(
                relationship.isActive());
    }

    @Test
    void shouldEndActiveCareRelationship() {

        // Arrange
        var command =
                new EstablishCareRelationshipCommand(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111"),
                        UUID.fromString(
                                "22222222-2222-2222-2222-222222222222"),
                        RelationshipType.CAREGIVER);

        var relationship =
                new CareRelationship(
                        command,
                        Instant.parse(
                                "2026-10-03T20:00:00Z"));

        var endedAt = Instant.parse(
                "2026-10-03T21:00:00Z");

        // Act
        relationship.end(endedAt);

        // Assert
        assertEquals(
                CareRelationshipStatus.ENDED,
                relationship.getStatus());

        assertEquals(
                endedAt,
                relationship.getEndedAt());

        assertFalse(
                relationship.isActive());
    }

    @Test
    void shouldRejectEndingRelationshipTwice() {

        // Arrange
        var command =
                new EstablishCareRelationshipCommand(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111"),
                        UUID.fromString(
                                "22222222-2222-2222-2222-222222222222"),
                        RelationshipType.FAMILY);

        var relationship =
                new CareRelationship(
                        command,
                        Instant.parse(
                                "2026-10-03T20:00:00Z"));

        relationship.end(
                Instant.parse(
                        "2026-10-03T21:00:00Z"));

        // Act
        var exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> relationship.end(
                                Instant.parse(
                                        "2026-10-03T22:00:00Z")));

        // Assert
        assertEquals(
                "care-relationship.already-ended",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullRelationshipType() {

        // Arrange
        var command =
                new EstablishCareRelationshipCommand(
                        UUID.fromString(
                                "11111111-1111-1111-1111-111111111111"),
                        UUID.fromString(
                                "22222222-2222-2222-2222-222222222222"),
                        null);

        var startedAt = Instant.parse(
                "2026-10-03T20:00:00Z");

        // Act
        var exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new CareRelationship(
                                command,
                                startedAt));

        // Assert
        assertEquals(
                "care-relationship.type.invalid",
                exception.getMessage());
    }
}