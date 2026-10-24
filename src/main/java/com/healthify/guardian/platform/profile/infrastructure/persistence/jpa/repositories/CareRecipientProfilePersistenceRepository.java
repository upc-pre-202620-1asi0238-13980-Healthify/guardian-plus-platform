package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.CareRecipientProfilePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for care recipient profile persistence entities.
 */
@Repository
public interface CareRecipientProfilePersistenceRepository
        extends JpaRepository<CareRecipientProfilePersistenceEntity, UUID> {

    List<CareRecipientProfilePersistenceEntity> findByCreatedByUserId(UserId userId);
}