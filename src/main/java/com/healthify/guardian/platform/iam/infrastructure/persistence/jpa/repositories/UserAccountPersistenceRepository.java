package com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.entities.UserAccountPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for user account persistence entities.
 */
@Repository
public interface UserAccountPersistenceRepository extends JpaRepository<UserAccountPersistenceEntity, UUID> {

    Optional<UserAccountPersistenceEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserAccountPersistenceEntity> findByPasswordResetTokenHash(String passwordResetTokenHash);
}
