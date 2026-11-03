package com.healthify.guardian.platform.emergencyalerting.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published by {@code emergencyAlerting} when a confirmed {@code CRITICAL} or
 * {@code HIGH} alert is first dispatched to the Care Circle.
 *
 * <p>This is the <em>published language</em> of this bounded context; other bounded contexts
 * should listen to this event rather than to the internal {@code AlertDispatchedEvent}.</p>
 *
 * @param alertId                the dispatched alert
 * @param careRecipientProfileId the Fragile Citizen the alert is about
 * @param sourceType             the kind of signal that raised the alert
 * @param severity               the alert's severity
 * @param dispatchedAt           when the alert was dispatched
 */
public record EmergencyDispatchedIntegrationEvent(
        UUID alertId,
        UUID careRecipientProfileId,
        String sourceType,
        String severity,
        Instant dispatchedAt) {
}
