package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Response payload of a vital sign type with its normal range and physical limits.
 */
public record VitalSignTypeResource(
        String code,
        String name,
        String unit,
        BigDecimal normalMinimum,
        BigDecimal normalMaximum,
        BigDecimal physicalMinimum,
        BigDecimal physicalMaximum
) {
}
