package com.healthify.guardian.platform.profile.application.internal.commandservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.commands.CreateUserProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileCommandServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private UserProfileCommandServiceImpl userProfileCommandService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        fixedClock = Clock.fixed(
                Instant.parse("2026-10-03T20:00:00Z"),
                ZoneOffset.UTC);

        userProfileCommandService =
                new UserProfileCommandServiceImpl(
                        userProfileRepository,
                        fixedClock);
    }

    @Test
    void shouldCreateUserProfileWhenUserHasNoProfile() {

        // Arrange
        var userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111");

        var command = new CreateUserProfileCommand(
                userId,
                "Juan",
                "Sanchez",
                "999999999",
                null);

        when(userProfileRepository.findByUserId(
                any(UserId.class)))
                .thenReturn(Optional.empty());

        when(userProfileRepository.save(
                any(UserProfile.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        // Act
        var result =
                userProfileCommandService.handle(command);

        // Assert
        assertTrue(result.isSuccess());

        var profile =
                result.toOptional().orElseThrow();

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
                Instant.parse("2026-10-03T20:00:00Z"),
                profile.getCreatedAt());

        verify(userProfileRepository)
                .findByUserId(any(UserId.class));

        verify(userProfileRepository)
                .save(any(UserProfile.class));
    }

    @Test
    void shouldReturnConflictWhenUserAlreadyHasProfile() {

        // Arrange
        var userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111");

        var command = new CreateUserProfileCommand(
                userId,
                "Juan",
                "Sanchez",
                "999999999",
                null);

        var existingProfile =
                new UserProfile(
                        command,
                        Instant.parse(
                                "2026-10-03T19:00:00Z"));

        when(userProfileRepository.findByUserId(
                any(UserId.class)))
                .thenReturn(Optional.of(existingProfile));

        // Act
        var result =
                userProfileCommandService.handle(command);

        // Assert
        assertTrue(result.isFailure());
        assertTrue(result.toOptional().isEmpty());

        verify(userProfileRepository)
                .findByUserId(any(UserId.class));

        verify(userProfileRepository, never())
                .save(any(UserProfile.class));
    }
}