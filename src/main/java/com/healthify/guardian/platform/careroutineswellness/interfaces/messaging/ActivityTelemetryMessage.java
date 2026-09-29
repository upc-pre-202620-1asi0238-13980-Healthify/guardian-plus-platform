package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * Raw activity/inactivity telemetry payload as reported by the wearable device.
 *
 * <p>This is the external, hardware-facing contract; {@link ActivityTelemetryConsumer} is the
 * Anti-Corruption Layer that translates it into this bounded context's own commands.</p>
 *
 * @param personUnderCareId the person wearing the device
 * @param inactive           true when the device reports the start of a prolonged-inactivity
 *                           episode, false when it reports that activity resumed
 * @param timestamp          when the device detected the transition
 */
public record ActivityTelemetryMessage(UUID personUnderCareId, boolean inactive, Instant timestamp) {
}
