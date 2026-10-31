package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Immutable closed numeric interval of a vital sign, expressed in the unit of its type.
 * Used both for the clinically normal range and for the physically possible limits.
 *
 * @param minimum lower bound, inclusive
 * @param maximum upper bound, inclusive
 */
public record VitalSignRange(BigDecimal minimum, BigDecimal maximum) {

    private static final String INVALID_MESSAGE_KEY = "vital-sign-range.invalid";

    public VitalSignRange {
        if (minimum == null || maximum == null || minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static VitalSignRange of(String minimum, String maximum) {
        return new VitalSignRange(new BigDecimal(minimum), new BigDecimal(maximum));
    }

    /**
     * Classifies a reading against this range; both bounds are inclusive.
     */
    public ReadingClassification classify(VitalSignValue value) {
        if (value.value().compareTo(minimum) < 0) {
            return ReadingClassification.BELOW_RANGE;
        }
        if (value.value().compareTo(maximum) > 0) {
            return ReadingClassification.ABOVE_RANGE;
        }
        return ReadingClassification.WITHIN_RANGE;
    }

    public boolean contains(VitalSignValue value) {
        return value != null && !classify(value).isOutOfRange();
    }

    public boolean encloses(VitalSignRange other) {
        return other != null && minimum.compareTo(other.minimum) <= 0 && maximum.compareTo(other.maximum) >= 0;
    }
}
