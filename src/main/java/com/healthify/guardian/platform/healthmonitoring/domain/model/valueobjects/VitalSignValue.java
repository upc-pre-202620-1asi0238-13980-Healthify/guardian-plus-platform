package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Raw numeric value of a single reading. It carries no unit or range of its own: its clinical
 * meaning depends on its {@code VitalSignType}.
 *
 * @param value the measured value
 */
public record VitalSignValue(BigDecimal value) {

    private static final String INVALID_MESSAGE_KEY = "vital-sign.value.invalid";

    public VitalSignValue {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
