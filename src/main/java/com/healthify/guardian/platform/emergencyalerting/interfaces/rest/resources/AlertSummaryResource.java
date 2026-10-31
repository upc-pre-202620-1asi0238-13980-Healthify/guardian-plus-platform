package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload summarizing an alert, for lists.
 *
 * @param id                     the alert identifier
 * @param careRecipientProfileId the Fragile Citizen the alert is about
 * @param sourceType             the kind of signal that raised it
 * @param severity               {@code CRITICAL}, {@code HIGH} or {@code MEDIUM}
 * @param status                 the lifecycle status
 * @param currentRecipientLevel  the furthest escalation level reached, if dispatched
 * @param triggeredAt            when the signal was detected
 * @param acknowledgedAt         when a recipient acknowledged it
 */
public record AlertSummaryResource(
        UUID id,
        UUID careRecipientProfileId,
        String sourceType,
        String severity,
        String status,
        String currentRecipientLevel,
        Instant triggeredAt,
        Instant acknowledgedAt
) {
}
