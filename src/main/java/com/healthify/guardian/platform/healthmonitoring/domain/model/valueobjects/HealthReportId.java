package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier of a {@code HealthReport} aggregate.
 *
 * @param value the underlying UUID
 */
public record HealthReportId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "health-report.id.invalid";

    public HealthReportId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static HealthReportId generate() {
        return new HealthReportId(UUID.randomUUID());
    }
}
