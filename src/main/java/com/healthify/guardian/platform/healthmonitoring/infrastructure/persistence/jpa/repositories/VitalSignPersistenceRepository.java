package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.VitalSignPersistenceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for vital sign reading persistence entities.
 */
@Repository
public interface VitalSignPersistenceRepository extends JpaRepository<VitalSignPersistenceEntity, UUID> {

    Optional<VitalSignPersistenceEntity> findFirstByCareRecipientProfileIdAndVitalSignTypeIdAndEmittedAtIsNotNullOrderByMeasuredAtDesc(
            CareRecipientProfileId careRecipientProfileId, UUID vitalSignTypeId);

    List<VitalSignPersistenceEntity> findByCareRecipientProfileIdAndVitalSignTypeIdOrderByMeasuredAtDesc(
            CareRecipientProfileId careRecipientProfileId, UUID vitalSignTypeId, Pageable pageable);

    List<VitalSignPersistenceEntity> findByCareRecipientProfileIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanOrderByMeasuredAtAsc(
            CareRecipientProfileId careRecipientProfileId, Instant from, Instant to);

    boolean existsByWearableDeviceIdAndVitalSignTypeIdAndMeasuredAt(UUID wearableDeviceId, UUID vitalSignTypeId, Instant measuredAt);
}
