package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.VitalSignTypePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for vital sign type persistence entities.
 */
@Repository
public interface VitalSignTypePersistenceRepository extends JpaRepository<VitalSignTypePersistenceEntity, UUID> {

    Optional<VitalSignTypePersistenceEntity> findByCode(String code);
}
