package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Immutable time-of-day interval configured for the person under care, used by
 * {@code Reminder} to decide whether a hydration reminder must be suppressed
 * because it would otherwise wake the person up.
 *
 * <p>The interval may wrap around midnight (e.g. 22:00 to 06:00).</p>
 *
 * @param start start of the sleep window, inclusive
 * @param end   end of the sleep window, exclusive
 */
public record SleepWindow(LocalTime start, LocalTime end) {

    private static final String INVALID_MESSAGE_KEY = "sleep-window.bounds.invalid";

    public SleepWindow {
        if (start == null || end == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    /**
     * Tells whether the given instant, expressed in the given zone, falls within this window.
     *
     * @param instant the instant to check
     * @param zone    the zone the instant should be interpreted in
     * @return true if the instant's local time falls within this (possibly overnight) window
     */
    public boolean contains(Instant instant, ZoneId zone) {
        var time = instant.atZone(zone).toLocalTime();
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        }
        // Overnight window (e.g. 22:00 -> 06:00): "inside" means at/after start OR before end.
        return !time.isBefore(start) || time.isBefore(end);
    }
}
