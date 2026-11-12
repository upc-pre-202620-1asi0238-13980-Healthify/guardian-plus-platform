package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable periodic reading of the wearable's kinematic sensors.
 *
 * @param measuredAt      when the wearable took the sample
 * @param steps           steps counted since the previous sample; zero when the person did not move
 * @param inactiveMinutes minutes without movement accumulated by the wearable up to this sample
 */
public record ActivitySample(Instant measuredAt, Integer steps, BigDecimal inactiveMinutes) {

    private static final String INVALID_MESSAGE_KEY = "activity-sample.invalid";

    public ActivitySample {
        if (measuredAt == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
        steps = steps == null ? 0 : steps;
        inactiveMinutes = inactiveMinutes == null ? BigDecimal.ZERO : inactiveMinutes;
        if (steps < 0 || inactiveMinutes.signum() < 0) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    /** True when the person moved since the previous sample. */
    public boolean isMoving() {
        return steps > 0;
    }
}
