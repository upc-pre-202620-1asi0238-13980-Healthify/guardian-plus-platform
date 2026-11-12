package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing one closed night of sleep.
 *
 * @param id                the sleep cycle record's unique identifier
 * @param personUnderCareId the person who slept
 * @param startTime         when the person fell asleep
 * @param endTime           when the person woke up
 * @param durationMinutes   total time asleep
 * @param interruptionCount times the person woke up during the night
 * @param continuityScore   how continuous the night was, from 0 to 100
 * @param classification    {@code REGULAR} or {@code FRAGMENTED} (more than 4 interruptions)
 */
public record SleepCycleRecordResource(
        UUID id,
        UUID personUnderCareId,
        Instant startTime,
        Instant endTime,
        long durationMinutes,
        Integer interruptionCount,
        int continuityScore,
        String classification
) {
}
