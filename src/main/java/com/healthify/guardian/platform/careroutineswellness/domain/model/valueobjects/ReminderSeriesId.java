package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier shared by every occurrence of the same (possibly recurring) reminder.
 *
 * @param value the underlying UUID; generated when the first occurrence is scheduled
 */
public record ReminderSeriesId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "reminder.series-id.invalid";

    public ReminderSeriesId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    /**
     * Generates a brand-new, random identifier for a series being started.
     */
    public static ReminderSeriesId generate() {
        return new ReminderSeriesId(UUID.randomUUID());
    }
}
