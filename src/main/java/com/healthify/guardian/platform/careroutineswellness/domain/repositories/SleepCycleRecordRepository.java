package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.util.List;

/**
 * Sleep cycle record aggregate repository port.
 */
public interface SleepCycleRecordRepository {

    /**
     * Retrieves every closed sleep cycle recorded for a person under care.
     *
     * @param personUnderCareId the person whose sleep cycles are requested
     * @return the matching sleep cycle records, most recent first
     */
    List<SleepCycleRecord> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);

    /**
     * Persists a sleep cycle record and publishes its registered domain events.
     *
     * @param record the sleep cycle record to save
     * @return the saved sleep cycle record
     */
    SleepCycleRecord save(SleepCycleRecord record);
}
