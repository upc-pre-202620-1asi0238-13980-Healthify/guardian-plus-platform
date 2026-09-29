package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.SleepCycleRecordCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordSleepCycleCommand;
import org.springframework.stereotype.Component;

/**
 * Anti-Corruption Layer that receives closed sleep-cycle telemetry from the wearable device
 * and translates it into this bounded context's own commands.
 *
 * <p>Wired to the physical broker by {@code WearableTelemetryBrokerAdapter}; kept independent
 * of any messaging technology so it can be invoked directly (e.g. by a wearable simulator)
 * once one is available.</p>
 */
@Component
public class SleepTelemetryConsumer {

    private final SleepCycleRecordCommandService sleepCycleRecordCommandService;

    public SleepTelemetryConsumer(SleepCycleRecordCommandService sleepCycleRecordCommandService) {
        this.sleepCycleRecordCommandService = sleepCycleRecordCommandService;
    }

    /**
     * Consumes a single closed sleep-cycle telemetry message.
     *
     * @param message the raw telemetry reported by the wearable device
     */
    public void consume(SleepTelemetryMessage message) {
        sleepCycleRecordCommandService.handle(new RecordSleepCycleCommand(
                message.personUnderCareId(), message.startTime(), message.endTime(), message.interruptionCount()));
    }
}
