package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.SleepCycleRecordPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for sleep cycle record persistence entities.
 */
@Repository
public interface SleepCycleRecordPersistenceRepository extends JpaRepository<SleepCycleRecordPersistenceEntity, UUID> {

    List<SleepCycleRecordPersistenceEntity> findByPersonUnderCareIdOrderByStartTimeDesc(PersonUnderCareId personUnderCareId);
}
