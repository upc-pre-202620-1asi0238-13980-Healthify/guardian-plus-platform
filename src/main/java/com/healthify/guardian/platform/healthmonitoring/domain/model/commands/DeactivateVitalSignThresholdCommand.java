package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.util.UUID;

/**
 * Command to stop evaluating readings against a threshold.
 */
public record DeactivateVitalSignThresholdCommand(UUID vitalSignThresholdId) {
}
