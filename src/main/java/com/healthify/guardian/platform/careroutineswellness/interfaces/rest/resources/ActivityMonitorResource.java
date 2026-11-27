package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Response payload with the current activity status of a person under care and how inactivity is watched.
 *
 * @param id                        the activity monitor's unique identifier
 * @param personUnderCareId         the person being monitored
 * @param status                    {@code NORMAL} or {@code INACTIVITY_DETECTED}
 * @param inactivitySince           when prolonged inactivity was detected, while it lasts
 * @param inactiveMinutes           current minutes without movement
 * @param lastMovementAt            when movement was last detected
 * @param lastSampleAt              when the wearable last reported activity
 * @param detectionEnabled          whether prolonged inactivity raises an alert
 * @param thresholdMinutes          minutes without movement after which an alert is raised
 * @param watchHoursStart           start of the daily watch hours (end of the sleep window)
 * @param watchHoursEnd             end of the daily watch hours (start of the sleep window)
 */
public record ActivityMonitorResource(
        UUID id,
        UUID personUnderCareId,
        String status,
        Instant inactivitySince,
        BigDecimal inactiveMinutes,
        Instant lastMovementAt,
        Instant lastSampleAt,
        boolean detectionEnabled,
        Integer thresholdMinutes,
        LocalTime watchHoursStart,
        LocalTime watchHoursEnd
) {
}
