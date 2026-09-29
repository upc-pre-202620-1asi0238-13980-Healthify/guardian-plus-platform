package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ActivityMonitorCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordActivityResumedCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordProlongedInactivityCommand;
import org.springframework.stereotype.Component;

/**
 * Anti-Corruption Layer that receives activity/inactivity telemetry from the wearable device
 * and translates it into this bounded context's own commands.
 *
 * <p>Wired to the physical broker by {@code WearableTelemetryBrokerAdapter}; kept independent
 * of any messaging technology so it can be invoked directly (e.g. by a wearable simulator)
 * once one is available.</p>
 */
@Component
public class ActivityTelemetryConsumer {

    private final ActivityMonitorCommandService activityMonitorCommandService;

    public ActivityTelemetryConsumer(ActivityMonitorCommandService activityMonitorCommandService) {
        this.activityMonitorCommandService = activityMonitorCommandService;
    }

    /**
     * Consumes a single activity/inactivity telemetry message.
     *
     * @param message the raw telemetry reported by the wearable device
     */
    public void consume(ActivityTelemetryMessage message) {
        if (message.inactive()) {
            activityMonitorCommandService.handle(
                    new RecordProlongedInactivityCommand(message.personUnderCareId(), message.timestamp()));
        } else {
            activityMonitorCommandService.handle(
                    new RecordActivityResumedCommand(message.personUnderCareId(), message.timestamp()));
        }
    }
}
