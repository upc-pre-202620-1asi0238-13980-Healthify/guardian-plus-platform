package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing an incident.
 *
 * @param id                  the incident identifier
 * @param alertId             the alert the incident attends
 * @param status              {@code IN_ATTENTION}, {@code STABILIZED} or {@code CLOSED}
 * @param markedInAttentionAt when the alert was acknowledged
 * @param stabilizedAt        when the situation was declared stabilized
 * @param closedAt            when the incident was closed
 * @param notes               the notes recorded at each stage
 */
public record IncidentResource(
        UUID id,
        UUID alertId,
        String status,
        Instant markedInAttentionAt,
        Instant stabilizedAt,
        Instant closedAt,
        String notes
) {
}
