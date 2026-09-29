package com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ActivityMonitorCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordActivityResumedCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordProlongedInactivityCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityMonitorRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

/**
 * Application service that executes activity monitor commands.
 *
 * <p>Both commands upsert: the monitor for a person under care is created on demand the first
 * time telemetry arrives for them, since the wearable stream never explicitly provisions one.</p>
 */
@Service
public class ActivityMonitorCommandServiceImpl implements ActivityMonitorCommandService {

    private final ActivityMonitorRepository activityMonitorRepository;

    public ActivityMonitorCommandServiceImpl(ActivityMonitorRepository activityMonitorRepository) {
        this.activityMonitorRepository = activityMonitorRepository;
    }

    @Override
    public Result<ActivityMonitor, ApplicationError> handle(RecordProlongedInactivityCommand command) {
        try {
            var monitor = findOrCreate(command.personUnderCareId());
            monitor.recordProlongedInactivity(command.detectedAt());
            return Result.success(activityMonitorRepository.save(monitor));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "record-prolonged-inactivity", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }

    @Override
    public Result<ActivityMonitor, ApplicationError> handle(RecordActivityResumedCommand command) {
        try {
            var monitor = findOrCreate(command.personUnderCareId());
            monitor.recordActivityResumed(command.resumedAt());
            return Result.success(activityMonitorRepository.save(monitor));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "record-activity-resumed", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }

    private ActivityMonitor findOrCreate(java.util.UUID rawPersonUnderCareId) {
        var personUnderCareId = new PersonUnderCareId(rawPersonUnderCareId);
        return activityMonitorRepository.findByPersonUnderCareId(personUnderCareId)
                .orElseGet(() -> new ActivityMonitor(personUnderCareId));
    }
}
