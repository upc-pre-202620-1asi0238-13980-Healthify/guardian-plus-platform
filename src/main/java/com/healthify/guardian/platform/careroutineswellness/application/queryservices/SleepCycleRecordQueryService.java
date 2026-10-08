package com.healthify.guardian.platform.careroutineswellness.application.queryservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetSleepCycleRecordsByPersonUnderCareIdQuery;

import java.util.List;

/**
 * Application service contract for sleep cycle record read queries.
 */
public interface SleepCycleRecordQueryService {

    /**
     * Handles retrieval of the closed sleep cycles of a person under care.
     *
     * @param query person-under-care-id query, optionally narrowed to a period
     * @return matching sleep cycle records, most recent first
     */
    List<SleepCycleRecord> handle(GetSleepCycleRecordsByPersonUnderCareIdQuery query);
}
