package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

import java.time.Duration;
import java.time.Instant;

/**
 * Window during which the Fragile Citizen can cancel a detected fall from the wearable before it is
 * confirmed and dispatched. A fixed policy value, never persisted.
 */
public final class FallConfirmationWindow {

    public static final Duration DURATION = Duration.ofSeconds(20);

    private FallConfirmationWindow() {
    }

    /**
     * Tells whether the window opened at {@code triggeredAt} has already closed.
     *
     * @param triggeredAt when the fall was detected
     * @param now         the current time
     * @return true if the window has elapsed
     */
    public static boolean hasExpired(Instant triggeredAt, Instant now) {
        return !triggeredAt.plus(DURATION).isAfter(now);
    }

    /**
     * Returns the latest triggering time whose window has already elapsed at {@code now}, used to
     * find alerts still pending confirmation that must be confirmed.
     *
     * @param now the current time
     * @return {@code now} minus the window duration
     */
    public static Instant expiredIfTriggeredBefore(Instant now) {
        return now.minus(DURATION);
    }
}
