package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.EmergencyContactPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for emergency contact persistence entities.
 */
@Repository
public interface EmergencyContactPersistenceRepository extends JpaRepository<EmergencyContactPersistenceEntity, UUID> {

    List<EmergencyContactPersistenceEntity> findByCareRecipientProfileIdAndActiveTrueOrderByPriorityOrderAsc(
            CareRecipientProfileId careRecipientProfileId);

    Optional<EmergencyContactPersistenceEntity> findByCareRecipientProfileIdAndUserId(
            CareRecipientProfileId careRecipientProfileId, UserId userId);
}
