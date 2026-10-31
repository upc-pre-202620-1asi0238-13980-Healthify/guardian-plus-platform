package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
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

    Optional<VitalSignPersistenceEntity> findFirstByCareRecipientProfileIdAndVitalSignTypeAndEmittedAtIsNotNullOrderByMeasuredAtDesc(
            CareRecipientProfileId careRecipientProfileId, VitalSignType vitalSignType);

    List<VitalSignPersistenceEntity> findByCareRecipientProfileIdAndVitalSignTypeOrderByMeasuredAtDesc(
            CareRecipientProfileId careRecipientProfileId, VitalSignType vitalSignType, Pageable pageable);

    List<VitalSignPersistenceEntity> findByCareRecipientProfileIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanOrderByMeasuredAtAsc(
            CareRecipientProfileId careRecipientProfileId, Instant from, Instant to);

    boolean existsByWearableDeviceIdAndVitalSignTypeAndMeasuredAt(UUID wearableDeviceId, VitalSignType vitalSignType, Instant measuredAt);
}
