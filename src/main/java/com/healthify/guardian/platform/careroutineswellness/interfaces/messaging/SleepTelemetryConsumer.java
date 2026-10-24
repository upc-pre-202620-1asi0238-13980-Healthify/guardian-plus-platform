package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.SleepCycleRecordCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordSleepCycleCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Optional;

/**
 * Anti-Corruption Layer that receives nightly sleep reports from the wearable device and translates them
 * into this bounded context's own commands.
 *
 * <p>The wearable reports the night as "hours asleep, ending now"; this layer turns it into the closed
 * interval the domain works with, ending at {@code measuredAt}.</p>
 */
@Component
public class SleepTelemetryConsumer {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

    private final SleepCycleRecordCommandService sleepCycleRecordCommandService;

    public SleepTelemetryConsumer(SleepCycleRecordCommandService sleepCycleRecordCommandService) {
        this.sleepCycleRecordCommandService = sleepCycleRecordCommandService;
    }

    /**
     * Consumes a single sleep telemetry message.
     *
     * @param message the raw telemetry reported by the wearable device
     * @return the result of the command, or empty when the message is not a closed sleep cycle
     * @throws IllegalArgumentException when a mandatory field is missing or the person id is not a UUID
     */
    public Optional<Result<SleepCycleRecord, ApplicationError>> consume(SleepTelemetryMessage message) {
        if (!message.isSleepCycleRecorded()) {
            return Optional.empty();
        }
        if (message.measuredAt() == null || message.sleepHours() == null) {
            throw new IllegalArgumentException("sleep cycle without measuredAt or sleepHours");
        }
        var endTime = message.measuredAt().toInstant();
        var minutesAsleep = message.sleepHours().multiply(MINUTES_PER_HOUR).setScale(0, RoundingMode.HALF_UP).longValue();
        return Optional.of(sleepCycleRecordCommandService.handle(new RecordSleepCycleCommand(
                TelemetryIdentifiers.toPersonUnderCareId(message.careRecipientProfileId()),
                endTime.minus(Duration.ofMinutes(minutesAsleep)),
                endTime,
                message.interruptions())));
    }
}
