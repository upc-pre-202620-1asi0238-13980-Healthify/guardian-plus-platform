package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.WearableDevicePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for wearable device persistence entities.
 */
@Repository
public interface WearableDevicePersistenceRepository extends JpaRepository<WearableDevicePersistenceEntity, UUID> {

    List<WearableDevicePersistenceEntity> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId);

    Optional<WearableDevicePersistenceEntity> findBySerialNumber(String serialNumber);
}
