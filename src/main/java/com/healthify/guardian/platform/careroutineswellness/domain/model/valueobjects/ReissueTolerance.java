package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.time.Duration;

/**
 * Immutable time an issued medication reminder waits for the person's confirmation before it is
 * reissued (business rule from the report: 10 minutes).
 *
 * @param duration how long to wait for the confirmation
 */
public record ReissueTolerance(Duration duration) {

    private static final String INVALID_MESSAGE_KEY = "reminder.reissue-tolerance.invalid";

    public ReissueTolerance {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
