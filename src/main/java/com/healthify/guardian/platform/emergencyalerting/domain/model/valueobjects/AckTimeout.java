package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

import java.time.Instant;

/**
 * Time to wait for a recipient's acknowledgement before an alert escalates to the next level.
 *
 * @param seconds the timeout, between {@value #MIN_SECONDS} and {@value #MAX_SECONDS} seconds
 */
public record AckTimeout(Integer seconds) {

    public static final int MIN_SECONDS = 15;
    public static final int MAX_SECONDS = 300;
    public static final int DEFAULT_SECONDS = 60;

    private static final String OUT_OF_RANGE_MESSAGE_KEY = "ack-timeout.out-of-range";

    public AckTimeout {
        if (seconds == null || seconds < MIN_SECONDS || seconds > MAX_SECONDS) {
            throw new IllegalArgumentException(OUT_OF_RANGE_MESSAGE_KEY);
        }
    }

    /** Returns the default timeout of {@value #DEFAULT_SECONDS} seconds. */
    public static AckTimeout defaultTimeout() {
        return new AckTimeout(DEFAULT_SECONDS);
    }

    /**
     * Tells whether this timeout has elapsed.
     *
     * @param since when the wait started
     * @param now   the current time
     * @return true if at least {@code seconds} have passed since {@code since}
     */
    public boolean hasExpired(Instant since, Instant now) {
        return since != null && !since.plusSeconds(seconds).isAfter(now);
    }
}
