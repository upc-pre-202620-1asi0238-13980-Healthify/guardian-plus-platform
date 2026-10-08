package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityMonitorId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Raised when an {@code ActivityMonitor} transitions from {@code NORMAL} to
 * {@code INACTIVITY_DETECTED}.
 *
 * <p>Republished by {@code ProlongedInactivityDetectedEventHandler} as a
 * {@code ProlongedInactivityDetectedIntegrationEvent} for {@code Emergency & Alerting}.</p>
 *
 * @param inactiveMinutes how long the person had been still when the threshold was reached
 */
public record ProlongedInactivityDetectedEvent(
        ActivityMonitorId activityMonitorId,
        PersonUnderCareId personUnderCareId,
        Instant detectedAt,
        BigDecimal inactiveMinutes) {

    public static ProlongedInactivityDetectedEvent from(ActivityMonitor monitor) {
        return new ProlongedInactivityDetectedEvent(
                monitor.getId(), monitor.getPersonUnderCareId(), monitor.getInactivitySince(), monitor.getInactiveMinutes());
    }
}
