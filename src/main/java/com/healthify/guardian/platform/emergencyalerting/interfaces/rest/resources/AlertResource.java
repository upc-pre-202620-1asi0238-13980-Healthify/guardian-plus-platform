package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response payload representing an alert with its deliveries and responses.
 *
 * @param id                     the alert identifier
 * @param careRecipientProfileId the Fragile Citizen the alert is about
 * @param sourceType             the kind of signal that raised it
 * @param sourceReferenceId      the record that originated the signal
 * @param severity               {@code CRITICAL}, {@code HIGH} or {@code MEDIUM}
 * @param status                 the lifecycle status
 * @param currentRecipientLevel  the furthest escalation level reached, if dispatched
 * @param triggeredAt            when the signal was detected
 * @param confirmedAt            when the alert was confirmed for dispatch
 * @param lastDispatchedAt       when its last escalation level was dispatched
 * @param acknowledgedAt         when a recipient acknowledged it
 * @param acknowledgedByUserId   the recipient who acknowledged it
 * @param resolvedAt             when it was definitively closed
 * @param deliveries             every delivery, in dispatch order
 * @param responses              every response of the Care Circle
 */
public record AlertResource(
        UUID id,
        UUID careRecipientProfileId,
        String sourceType,
        UUID sourceReferenceId,
        String severity,
        String status,
        String currentRecipientLevel,
        Instant triggeredAt,
        Instant confirmedAt,
        Instant lastDispatchedAt,
        Instant acknowledgedAt,
        UUID acknowledgedByUserId,
        Instant resolvedAt,
        List<AlertDeliveryResource> deliveries,
        List<AlertResponseResource> responses
) {
}
