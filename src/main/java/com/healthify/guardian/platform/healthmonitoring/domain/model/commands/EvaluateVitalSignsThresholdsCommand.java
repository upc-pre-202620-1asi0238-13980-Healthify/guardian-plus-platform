package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.util.UUID;

/**
 * Command to evaluate an emitted vital sign against the threshold in force for its care recipient and type.
 */
public record EvaluateVitalSignsThresholdsCommand(UUID vitalSignId) {
}
