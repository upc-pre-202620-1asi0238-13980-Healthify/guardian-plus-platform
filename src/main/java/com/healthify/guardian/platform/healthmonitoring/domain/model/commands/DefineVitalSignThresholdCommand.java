package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to define (or redefine) the clinical range of a vital sign type for a care recipient.
 */
public record DefineVitalSignThresholdCommand(
        UUID careRecipientProfileId,
        UUID vitalSignTypeId,
        BigDecimal minimumValue,
        BigDecimal maximumValue,
        Integer requiredConsecutiveHits) {
}
