package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to change how prolonged inactivity is detected for a person under care.
 *
 * @param personUnderCareId the person whose activity monitor must be configured
 * @param enabled           whether prolonged inactivity raises an alert
 * @param thresholdMinutes  minutes without movement after which an alert is raised
 */
public record ConfigureInactivityDetectionCommand(UUID personUnderCareId, boolean enabled, Integer thresholdMinutes) {
}
