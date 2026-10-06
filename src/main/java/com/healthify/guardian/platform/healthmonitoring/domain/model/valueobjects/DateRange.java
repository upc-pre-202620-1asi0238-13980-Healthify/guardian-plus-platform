package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.time.LocalDate;

/**
 * Immutable closed period of days.
 *
 * @param startDate first day, inclusive
 * @param endDate   last day, inclusive
 */
public record DateRange(LocalDate startDate, LocalDate endDate) {

    private static final String INVALID_MESSAGE_KEY = "date-range.invalid";

    public DateRange {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(startDate) && !date.isAfter(endDate);

    } 
}
