package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityMonitorId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.Instant;

/**
 * Raised when an {@code ActivityMonitor} is reset back to {@code NORMAL} after a
 * prolonged-inactivity episode.
 */
public record ActivityResumedEvent(
        ActivityMonitorId activityMonitorId,
        PersonUnderCareId personUnderCareId,
        Instant resumedAt) {

    public static ActivityResumedEvent from(ActivityMonitor monitor, Instant resumedAt) {
        return new ActivityResumedEvent(monitor.getId(), monitor.getPersonUnderCareId(), resumedAt);
    }
}
