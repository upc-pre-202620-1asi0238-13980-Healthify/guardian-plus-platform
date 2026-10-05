package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.VitalSignThresholdPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for vital sign threshold persistence entities.
 */
@Repository
public interface VitalSignThresholdPersistenceRepository extends JpaRepository<VitalSignThresholdPersistenceEntity, UUID> {

    Optional<VitalSignThresholdPersistenceEntity> findByCareRecipientProfileIdAndVitalSignTypeId(
            CareRecipientProfileId careRecipientProfileId, UUID vitalSignTypeId);

    List<VitalSignThresholdPersistenceEntity> findByCareRecipientProfileIdAndActiveTrue(CareRecipientProfileId careRecipientProfileId);
}
