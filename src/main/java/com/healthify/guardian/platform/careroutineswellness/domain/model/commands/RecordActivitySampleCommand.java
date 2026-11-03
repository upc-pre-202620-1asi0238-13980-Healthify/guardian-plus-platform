package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Command to apply a periodic kinematic sample reported by the wearable device to the person's
 * {@code ActivityMonitor}.
 *
 * @param personUnderCareId the person wearing the device
 * @param measuredAt        when the wearable took the sample
 * @param steps             steps counted since the previous sample
 * @param inactiveMinutes   minutes without movement accumulated by the wearable up to this sample
 */
public record RecordActivitySampleCommand(UUID personUnderCareId, Instant measuredAt, Integer steps, BigDecimal inactiveMinutes) {
}
