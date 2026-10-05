package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.math.BigDecimal;

/**
 * Command to register a new vital sign type in the catalog, together with its clinically normal
 * range and the physically possible limits of any reading of that type.
 */
public record RegisterVitalSignTypeCommand(String code,
                                           String name,
                                           String unit,
                                           BigDecimal normalMinimum,
                                           BigDecimal normalMaximum,
                                           BigDecimal physicalMinimum,
                                           BigDecimal physicalMaximum) {
}
