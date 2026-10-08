package com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ActivityMonitorCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfigureInactivityDetectionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordActivitySampleCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivitySample;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.InactivityDetectionSettings;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityMonitorRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.UUID;

/**
 * Application service that executes activity monitor commands.
 *
 * <p>Both commands upsert: the monitor for a person under care is created on demand the first time
 * telemetry or configuration arrives for them, since the wearable stream never explicitly provisions one.</p>
 */
@Service
public class ActivityMonitorCommandServiceImpl implements ActivityMonitorCommandService {

    private final ActivityMonitorRepository activityMonitorRepository;
    private final SleepWindow defaultSleepWindow;
    private final ZoneId zone;

    public ActivityMonitorCommandServiceImpl(
            ActivityMonitorRepository activityMonitorRepository,
            SleepWindow careRoutinesWellnessDefaultSleepWindow,
            ZoneId careRoutinesWellnessZoneId) {
        this.activityMonitorRepository = activityMonitorRepository;
        this.defaultSleepWindow = careRoutinesWellnessDefaultSleepWindow;
        this.zone = careRoutinesWellnessZoneId;
    }

    @Override
    public Result<ActivityMonitor, ApplicationError> handle(RecordActivitySampleCommand command) {
        try {
            var monitor = findOrCreate(command.personUnderCareId());
            monitor.recordSample(
                    new ActivitySample(command.measuredAt(), command.steps(), command.inactiveMinutes()),
                    defaultSleepWindow, zone);
            return Result.success(activityMonitorRepository.save(monitor));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "record-activity-sample", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }

    @Override
    public Result<ActivityMonitor, ApplicationError> handle(ConfigureInactivityDetectionCommand command) {
        try {
            var monitor = findOrCreate(command.personUnderCareId());
            monitor.configureDetection(new InactivityDetectionSettings(command.enabled(), command.thresholdMinutes()));
            return Result.success(activityMonitorRepository.save(monitor));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "configure-inactivity-detection", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }

    private ActivityMonitor findOrCreate(UUID rawPersonUnderCareId) {
        var personUnderCareId = new PersonUnderCareId(rawPersonUnderCareId);
        return activityMonitorRepository.findByPersonUnderCareId(personUnderCareId)
                .orElseGet(() -> new ActivityMonitor(personUnderCareId));
    }
}
