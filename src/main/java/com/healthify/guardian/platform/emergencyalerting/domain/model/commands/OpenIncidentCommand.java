package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to open the incident that tracks the human attention of an acknowledged alert.
 *
 * @param alertId             the acknowledged alert
 * @param markedInAttentionAt when the alert was acknowledged
 */
public record OpenIncidentCommand(UUID alertId, Instant markedInAttentionAt) {
}
