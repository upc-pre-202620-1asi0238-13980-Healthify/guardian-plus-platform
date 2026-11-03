package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request payload of a batch of readings synchronized from the offline buffer of a wearable (TS02, US21).
 */
public record TelemetryBatchResource(
        @NotEmpty(message = "{vital-sign.batch.empty}")
        List<@Valid DetectVitalSignsResource> readings
) {
}
