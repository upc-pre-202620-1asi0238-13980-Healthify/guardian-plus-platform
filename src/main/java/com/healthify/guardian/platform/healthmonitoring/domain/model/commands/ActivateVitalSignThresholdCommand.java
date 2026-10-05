package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.util.UUID;

/**
 * Command to put a previously deactivated threshold back in force.
 */
public record ActivateVitalSignThresholdCommand(UUID vitalSignThresholdId) {
}
