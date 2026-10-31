package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertSettingsPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for alert settings persistence entities.
 */
@Repository
public interface AlertSettingsPersistenceRepository extends JpaRepository<AlertSettingsPersistenceEntity, UUID> {

    Optional<AlertSettingsPersistenceEntity> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId);
}
