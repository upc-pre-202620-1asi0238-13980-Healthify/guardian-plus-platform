package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Immutable days-of-supply threshold at or below which a medication stock should be replenished
 * (business rule from the report: 3 days).
 *
 * @param days remaining days of supply that trigger a restock suggestion
 */
public record RestockThreshold(BigDecimal days) {

    private static final String INVALID_MESSAGE_KEY = "medication-stock.restock-threshold.invalid";

    public RestockThreshold {
        if (days == null || days.signum() < 0) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
