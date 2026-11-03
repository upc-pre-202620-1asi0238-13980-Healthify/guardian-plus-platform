package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.UserProfilePersistenceEntity;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories.UserProfilePersistenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileRepositoryImplTest {

    @Mock
    private UserProfilePersistenceRepository persistenceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UserProfileRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new UserProfileRepositoryImpl(
                persistenceRepository,
                eventPublisher);
    }

    @Test
    void shouldReuseExistingPersistenceEntityWhenUpdatingProfile() {

        var profileId =
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111111");

        var userId =
                new UserId(
                        UUID.fromString(
                                "22222222-2222-2222-2222-222222222222"));

        var createdAt =
                Instant.parse(
                        "2026-10-20T10:00:00Z");

        var updatedAt =
                Instant.parse(
                        "2026-10-20T11:00:00Z");

        var existingEntity =
                new UserProfilePersistenceEntity();

        existingEntity.setId(profileId);
        existingEntity.setUserId(userId);
        existingEntity.setFirstName("Juan");
        existingEntity.setLastName("Sanchez");
        existingEntity.setPhoneNumber("900000000");

        ReflectionTestUtils.setField(
                existingEntity,
                "createdAt",
                Date.from(createdAt));

        ReflectionTestUtils.setField(
                existingEntity,
                "updatedAt",
                Date.from(updatedAt));

        var profile = new UserProfile();

        profile.setId(
                new UserProfileId(profileId));

        profile.setUserId(userId);
        profile.setFirstName("Juan Antonio");
        profile.setLastName("Sanchez");
        profile.setPhoneNumber("999999999");

        when(
                persistenceRepository.findById(profileId))
                .thenReturn(
                        Optional.of(existingEntity));

        when(
                persistenceRepository.save(existingEntity))
                .thenReturn(existingEntity);

        var saved =
                repository.save(profile);

        verify(persistenceRepository)
                .save(same(existingEntity));

        assertEquals(
                createdAt,
                saved.getCreatedAt());

        assertEquals(
                "Juan Antonio",
                saved.getFirstName());

        assertEquals(
                "999999999",
                saved.getPhoneNumber());

        assertEquals(
                Date.from(createdAt),
                existingEntity.getCreatedAt());
    }
}