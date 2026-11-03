package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing a Care Circle member's response to an alert.
 *
 * @param id              the response identifier
 * @param responderUserId the member who took charge
 * @param responseStatus  {@code CLAIMED}, {@code COMPLETED} or {@code CANCELLED}
 * @param claimedAt       when the member took charge
 * @param completedAt     when the member recorded the outcome
 * @param notes           the outcome of the intervention
 */
public record AlertResponseResource(
        UUID id,
        UUID responderUserId,
        String responseStatus,
        Instant claimedAt,
        Instant completedAt,
        String notes
) {
}
