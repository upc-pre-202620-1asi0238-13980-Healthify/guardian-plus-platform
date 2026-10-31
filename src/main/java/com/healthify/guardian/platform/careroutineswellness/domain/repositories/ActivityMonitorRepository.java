package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.util.Optional;

/**
 * Activity monitor aggregate repository port.
 */
public interface ActivityMonitorRepository {

    /**
     * Retrieves the single activity monitor for a person under care.
     *
     * @param personUnderCareId the person whose activity monitor is requested
     * @return the matching activity monitor, if one has been created yet
     */
    Optional<ActivityMonitor> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);

    /**
     * Persists an activity monitor (create or update) and publishes its registered domain events.
     *
     * @param monitor the activity monitor to save
     * @return the saved activity monitor
     */
    ActivityMonitor save(ActivityMonitor monitor);
}
