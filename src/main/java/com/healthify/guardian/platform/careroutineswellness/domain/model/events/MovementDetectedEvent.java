package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityMonitorId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Raised when the person under care moves again after a long still period: the inactivity counter is
 * reset to zero without raising any alarm.
 *
 * @param previousInactiveMinutes how long the person had been still before moving
 */
public record MovementDetectedEvent(
        ActivityMonitorId activityMonitorId,
        PersonUnderCareId personUnderCareId,
        Instant detectedAt,
        BigDecimal previousInactiveMinutes) {

    public static MovementDetectedEvent from(ActivityMonitor monitor, Instant detectedAt, BigDecimal previousInactiveMinutes) {
        return new MovementDetectedEvent(monitor.getId(), monitor.getPersonUnderCareId(), detectedAt, previousInactiveMinutes);
    }
}
