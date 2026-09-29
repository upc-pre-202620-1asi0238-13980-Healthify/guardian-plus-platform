package com.healthify.guardian.platform.careroutineswellness.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published by {@code careRoutinesWellness} when a person under care's
 * {@code ActivityMonitor} detects prolonged physical inactivity.
 *
 * <p>This is the <em>published language</em> of this bounded context towards
 * {@code Emergency & Alerting}, which consumes it to trigger a {@code PROLONGED_INACTIVITY}
 * alert of {@code HIGH} severity. Other bounded contexts should listen to this event rather
 * than to the internal {@code ProlongedInactivityDetectedEvent}.</p>
 *
 * @param personUnderCareId the person whose inactivity was detected
 * @param detectedAt         when the prolonged inactivity was detected
 */
public record ProlongedInactivityDetectedIntegrationEvent(UUID personUnderCareId, Instant detectedAt) {
}
