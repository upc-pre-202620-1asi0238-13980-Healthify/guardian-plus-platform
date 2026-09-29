package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * Raw closed sleep-cycle telemetry payload as reported by the wearable device.
 *
 * <p>This is the external, hardware-facing contract; {@link SleepTelemetryConsumer} is the
 * Anti-Corruption Layer that translates it into this bounded context's own commands.</p>
 *
 * @param personUnderCareId the person wearing the device
 * @param startTime          when the sleep cycle started
 * @param endTime            when the sleep cycle ended
 * @param interruptionCount  number of interruptions detected during the cycle
 */
public record SleepTelemetryMessage(UUID personUnderCareId, Instant startTime, Instant endTime, Integer interruptionCount) {
}
