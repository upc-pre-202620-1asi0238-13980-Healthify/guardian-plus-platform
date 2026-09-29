package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ReminderPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for reminder persistence entities.
 */
@Repository
public interface ReminderPersistenceRepository extends JpaRepository<ReminderPersistenceEntity, UUID> {

    List<ReminderPersistenceEntity> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);

    List<ReminderPersistenceEntity> findByStatusAndScheduledTimeLessThanEqual(ReminderStatus status, Instant scheduledTime);

    List<ReminderPersistenceEntity> findByStatus(ReminderStatus status);
}
