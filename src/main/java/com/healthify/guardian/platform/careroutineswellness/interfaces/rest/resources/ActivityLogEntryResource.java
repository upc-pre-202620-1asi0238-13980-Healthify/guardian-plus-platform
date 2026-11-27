package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing one entry of the recent activity log.
 *
 * @param id              the entry's unique identifier
 * @param type            {@code MOVEMENT_DETECTED}, {@code WALK_DETECTED} or {@code PROLONGED_INACTIVITY_DETECTED}
 * @param occurredAt      when it happened (start of the walk, for walks)
 * @param durationMinutes walk length, for walks
 * @param steps           steps counted, for walks
 * @param inactiveMinutes minutes the person had been still, for movement and inactivity entries
 */
public record ActivityLogEntryResource(
        UUID id,
        String type,
        Instant occurredAt,
        Integer durationMinutes,
        Integer steps,
        BigDecimal inactiveMinutes
) {
}
