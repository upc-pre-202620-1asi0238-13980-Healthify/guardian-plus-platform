package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to record a closed sleep cycle reported by the wearable device.
 *
 * @param personUnderCareId the person the sleep cycle belongs to
 * @param startTime          when the sleep cycle started
 * @param endTime            when the sleep cycle ended
 * @param interruptionCount  number of interruptions detected during the cycle
 */
public record RecordSleepCycleCommand(UUID personUnderCareId, Instant startTime, Instant endTime, Integer interruptionCount) {
}
