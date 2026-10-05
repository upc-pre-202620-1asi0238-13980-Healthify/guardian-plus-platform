package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

/**
 * Where a vital sign reading falls with respect to the clinical range of its threshold.
 */
public enum ReadingClassification {
    BELOW_RANGE,
    WITHIN_RANGE,
    ABOVE_RANGE;

    public boolean isOutOfRange() {
        return this != WITHIN_RANGE;
    }
}
