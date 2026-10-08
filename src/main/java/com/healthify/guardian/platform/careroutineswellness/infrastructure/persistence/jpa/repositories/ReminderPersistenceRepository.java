package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ReminderPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for reminder persistence entities.
 */
@Repository
public interface ReminderPersistenceRepository extends JpaRepository<ReminderPersistenceEntity, UUID> {

    List<ReminderPersistenceEntity> findByPersonUnderCareIdAndScheduledTimeGreaterThanEqualAndScheduledTimeLessThanOrderByScheduledTime(
            PersonUnderCareId personUnderCareId, Instant from, Instant to);

    List<ReminderPersistenceEntity> findByPersonUnderCareIdAndTypeAndScheduledTimeGreaterThanEqualAndScheduledTimeLessThanOrderByScheduledTime(
            PersonUnderCareId personUnderCareId, ReminderType type, Instant from, Instant to);

    List<ReminderPersistenceEntity> findByPersonUnderCareIdAndTypeAndStatusInOrderByScheduledTime(
            PersonUnderCareId personUnderCareId, ReminderType type, Collection<ReminderStatus> statuses);

    /** Reminders created before {@code notifyAt} existed have no lead time, so they fall back to their scheduled time. */
    @Query("""
            select r from ReminderPersistenceEntity r
            where r.status = :status and coalesce(r.notifyAt, r.scheduledTime) <= :currentTime
            """)
    List<ReminderPersistenceEntity> findDueForIssuance(
            @Param("status") ReminderStatus status, @Param("currentTime") Instant currentTime);

    List<ReminderPersistenceEntity> findByStatus(ReminderStatus status);
}
