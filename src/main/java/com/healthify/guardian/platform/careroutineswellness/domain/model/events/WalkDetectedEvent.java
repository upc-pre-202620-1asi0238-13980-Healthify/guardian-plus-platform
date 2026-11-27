package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityMonitorId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.Instant;

/**
 * Raised when a sustained burst of movement (a walk) finishes.
 *
 * @param startedAt when the person started moving
 * @param endedAt   when the person was first seen still again
 * @param steps     steps counted during the walk
 */
public record WalkDetectedEvent(
        ActivityMonitorId activityMonitorId,
        PersonUnderCareId personUnderCareId,
        Instant startedAt,
        Instant endedAt,
        Integer steps) {

    public static WalkDetectedEvent from(ActivityMonitor monitor, Instant startedAt, Instant endedAt, Integer steps) {
        return new WalkDetectedEvent(monitor.getId(), monitor.getPersonUnderCareId(), startedAt, endedAt, steps);
    }
}
