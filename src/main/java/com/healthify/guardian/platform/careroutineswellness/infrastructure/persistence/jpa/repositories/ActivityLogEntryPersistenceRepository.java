package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ActivityLogEntryPersistenceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for activity log entry persistence entities.
 */
@Repository
public interface ActivityLogEntryPersistenceRepository extends JpaRepository<ActivityLogEntryPersistenceEntity, UUID> {

    List<ActivityLogEntryPersistenceEntity> findByPersonUnderCareIdOrderByOccurredAtDesc(
            PersonUnderCareId personUnderCareId, Pageable pageable);
}
