package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.Instant;
import java.util.List;

/**
 * Sleep cycle record aggregate repository port.
 */
public interface SleepCycleRecordRepository {

    /**
     * Retrieves the closed sleep cycles recorded for a person under care, optionally narrowed to the cycles
     * that ended within a period.
     *
     * @param personUnderCareId the person whose sleep cycles are requested
     * @param from              earliest end time, inclusive; unbounded when null
     * @param to                latest end time, exclusive; unbounded when null
     * @return the matching sleep cycle records, most recent first
     */
    List<SleepCycleRecord> findByPersonUnderCareId(PersonUnderCareId personUnderCareId, Instant from, Instant to);

    /**
     * Persists a sleep cycle record and publishes its registered domain events.
     *
     * @param record the sleep cycle record to save
     * @return the saved sleep cycle record
     */
    SleepCycleRecord save(SleepCycleRecord record);
}
