package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

/**
 * Where a vital sign reading falls with respect to the normal range of its vital sign type.
 */
public enum ReadingClassification {
    BELOW_RANGE,
    WITHIN_RANGE,
    ABOVE_RANGE;

    public boolean isOutOfRange() {
        return this != WITHIN_RANGE;
    }
}
