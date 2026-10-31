package com.healthify.guardian.platform.careroutineswellness.application.internal.queryservices;

import com.healthify.guardian.platform.careroutineswellness.application.queryservices.ActivityMonitorQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetActivityLogEntriesByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetActivityMonitorByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityLogEntryRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityMonitorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that resolves activity monitor and activity log read queries.
 */
@Service
public class ActivityMonitorQueryServiceImpl implements ActivityMonitorQueryService {

    private final ActivityMonitorRepository activityMonitorRepository;
    private final ActivityLogEntryRepository activityLogEntryRepository;

    public ActivityMonitorQueryServiceImpl(
            ActivityMonitorRepository activityMonitorRepository,
            ActivityLogEntryRepository activityLogEntryRepository) {
        this.activityMonitorRepository = activityMonitorRepository;
        this.activityLogEntryRepository = activityLogEntryRepository;
    }

    @Override
    public Optional<ActivityMonitor> handle(GetActivityMonitorByPersonUnderCareIdQuery query) {
        return activityMonitorRepository.findByPersonUnderCareId(query.personUnderCareId());
    }

    @Override
    public List<ActivityLogEntry> handle(GetActivityLogEntriesByPersonUnderCareIdQuery query) {
        return activityLogEntryRepository.findRecentByPersonUnderCareId(query.personUnderCareId(), query.limit());
    }
}
