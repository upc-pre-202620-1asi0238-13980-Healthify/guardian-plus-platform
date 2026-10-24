package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipStatus;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.CareRelationshipPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for care relationship persistence entities.
 */
@Repository
public interface CareRelationshipPersistenceRepository
        extends JpaRepository<CareRelationshipPersistenceEntity, UUID> {

    List<CareRelationshipPersistenceEntity> findByUserIdAndStatus(
            UserId userId,
            CareRelationshipStatus status);

    List<CareRelationshipPersistenceEntity>
    findByCareRecipientProfileIdAndStatus(
            CareRecipientProfileId careRecipientProfileId,
            CareRelationshipStatus status);
}