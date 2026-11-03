package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

import java.time.Instant;

/**
 * Optional time period used to filter an alert history. Either bound may be {@code null}, which
 * leaves that side of the period open.
 *
 * @param from start of the period, inclusive
 * @param to   end of the period, inclusive
 */
public record DateRange(Instant from, Instant to) {

    private static final String INVALID_MESSAGE_KEY = "date-range.invalid";

    public DateRange {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    /** Returns a period open on both sides. */
    public static DateRange unbounded() {
        return new DateRange(null, null);
    }
}
