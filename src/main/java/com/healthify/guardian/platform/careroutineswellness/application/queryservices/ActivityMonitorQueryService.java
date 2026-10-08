package com.healthify.guardian.platform.careroutineswellness.application.queryservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetActivityLogEntriesByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetActivityMonitorByPersonUnderCareIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for activity monitor and activity log read queries.
 */
public interface ActivityMonitorQueryService {

    /**
     * Handles retrieval of the current activity status of a person under care.
     *
     * @param query person-under-care-id query
     * @return matching activity monitor, if telemetry or configuration was ever received for the person
     */
    Optional<ActivityMonitor> handle(GetActivityMonitorByPersonUnderCareIdQuery query);

    /**
     * Handles retrieval of the most recent activity log entries of a person under care.
     *
     * @param query person-under-care-id query and maximum number of entries
     * @return matching entries, most recent first
     */
    List<ActivityLogEntry> handle(GetActivityLogEntriesByPersonUnderCareIdQuery query);
}
