package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.UserProfilePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for user profile persistence entities.
 */
@Repository
public interface UserProfilePersistenceRepository
        extends JpaRepository<UserProfilePersistenceEntity, UUID> {

    Optional<UserProfilePersistenceEntity> findByUserId(UserId userId);
}