package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.UserPreferencesPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for user preferences.
 */
@Repository
public interface UserPreferencesPersistenceRepository
        extends JpaRepository<UserPreferencesPersistenceEntity, UUID> {
}