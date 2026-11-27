package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ActivityMonitorPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for activity monitor persistence entities.
 */
@Repository
public interface ActivityMonitorPersistenceRepository extends JpaRepository<ActivityMonitorPersistenceEntity, UUID> {

    Optional<ActivityMonitorPersistenceEntity> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);
}
