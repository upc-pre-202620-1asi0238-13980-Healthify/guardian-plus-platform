package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.util.List;

/**
 * Activity log entry aggregate repository port.
 */
public interface ActivityLogEntryRepository {

    /**
     * Retrieves the most recent activity log entries of a person under care.
     *
     * @param personUnderCareId the person whose activity log is requested
     * @param limit             maximum number of entries to return
     * @return the matching entries, most recent first
     */
    List<ActivityLogEntry> findRecentByPersonUnderCareId(PersonUnderCareId personUnderCareId, int limit);

    /**
     * Persists an activity log entry and publishes its registered domain events.
     *
     * @param entry the activity log entry to save
     * @return the saved activity log entry
     */
    ActivityLogEntry save(ActivityLogEntry entry);
}
