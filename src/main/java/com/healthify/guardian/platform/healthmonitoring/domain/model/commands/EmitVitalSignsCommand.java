package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.util.UUID;

/**
 * Command to publish a detected vital sign for live consumption.
 */
public record EmitVitalSignsCommand(UUID vitalSignId) {
}
