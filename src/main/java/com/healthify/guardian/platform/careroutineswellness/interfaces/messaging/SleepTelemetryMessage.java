package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Wire format of the nightly sleep report published by the wearable (or the IoT simulator) on
 * {@code guardian/sleep/<deviceId>}.
 *
 * <p>This is the external, hardware-facing contract; {@link SleepTelemetryConsumer} is the
 * Anti-Corruption Layer that translates it into this bounded context's own commands. The wearable's own
 * classification and continuity index are ignored: the domain recomputes both.</p>
 *
 * @param deviceId               identifier of the wearable device, only used for logging
 * @param careRecipientProfileId the person wearing the device
 * @param signalType             only {@code SLEEP_CYCLE_RECORDED} carries a closed sleep cycle
 * @param measuredAt             when the night was closed (the wake-up time), with its offset
 * @param sleepHours             total hours asleep
 * @param interruptions          number of times the person woke up
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SleepTelemetryMessage(
        String deviceId,
        String careRecipientProfileId,
        String signalType,
        OffsetDateTime measuredAt,
        BigDecimal sleepHours,
        Integer interruptions) {

    public static final String SLEEP_CYCLE_RECORDED = "SLEEP_CYCLE_RECORDED";

    public boolean isSleepCycleRecorded() {
        return SLEEP_CYCLE_RECORDED.equals(signalType);
    }
}
