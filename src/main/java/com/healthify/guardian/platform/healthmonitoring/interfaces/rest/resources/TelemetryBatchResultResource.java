package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import java.util.List;

/**
 * Response payload of an accepted telemetry batch: how many readings arrived, were stored or skipped as duplicates.
 */
public record TelemetryBatchResultResource(
        int received,
        int stored,
        int duplicatesSkipped,
        List<VitalSignResource> vitalSigns
) {
}
