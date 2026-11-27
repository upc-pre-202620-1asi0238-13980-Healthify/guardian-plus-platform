package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ActivityMonitorCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordActivitySampleCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Anti-Corruption Layer that receives activity telemetry from the wearable device and translates it into
 * this bounded context's own commands.
 *
 * <p>Independent of any messaging technology: {@code WearableTelemetryMqttSubscriber} feeds it from the
 * broker, and tests can invoke it directly.</p>
 */
@Component
public class ActivityTelemetryConsumer {

    private final ActivityMonitorCommandService activityMonitorCommandService;

    public ActivityTelemetryConsumer(ActivityMonitorCommandService activityMonitorCommandService) {
        this.activityMonitorCommandService = activityMonitorCommandService;
    }

    /**
     * Consumes a single activity telemetry message.
     *
     * @param message the raw telemetry reported by the wearable device
     * @return the result of the command, or empty when the message is not an activity sample
     * @throws IllegalArgumentException when a mandatory field is missing or the person id is not a UUID
     */
    public Optional<Result<ActivityMonitor, ApplicationError>> consume(ActivityTelemetryMessage message) {
        if (!message.isActivitySample()) {
            return Optional.empty();
        }
        if (message.measuredAt() == null) {
            throw new IllegalArgumentException("activity sample without measuredAt");
        }
        return Optional.of(activityMonitorCommandService.handle(new RecordActivitySampleCommand(
                TelemetryIdentifiers.toPersonUnderCareId(message.careRecipientProfileId()),
                message.measuredAt().toInstant(),
                message.steps(),
                message.inactiveMinutes())));
    }
}
