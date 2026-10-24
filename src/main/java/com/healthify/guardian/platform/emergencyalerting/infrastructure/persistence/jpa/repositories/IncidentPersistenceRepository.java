package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.IncidentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for incident persistence entities.
 */
@Repository
public interface IncidentPersistenceRepository extends JpaRepository<IncidentPersistenceEntity, UUID> {

    Optional<IncidentPersistenceEntity> findByAlertId(UUID alertId);

    boolean existsByAlertId(UUID alertId);
}
