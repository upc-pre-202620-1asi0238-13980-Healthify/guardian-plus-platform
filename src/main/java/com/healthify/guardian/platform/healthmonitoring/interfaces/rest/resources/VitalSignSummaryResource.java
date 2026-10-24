package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

/**
 * Summary of one vital sign type inside a health report.
 */
public record VitalSignSummaryResource(
        Long id,
        String metricType,
        Double averageValue,
        Double minValue,
        Double maxValue,
        Integer readingsCount,
        Integer outOfRangeCount,
        String stabilityIndex
) {
}
