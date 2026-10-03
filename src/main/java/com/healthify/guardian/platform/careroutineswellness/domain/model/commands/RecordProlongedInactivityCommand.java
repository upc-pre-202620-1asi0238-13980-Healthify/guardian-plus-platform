package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to record that a person under care has been physically inactive for too long,
 * as reported by the wearable device's kinematic sensors.
 *
 * @param personUnderCareId the person whose activity monitor must transition
 * @param detectedAt         when the prolonged inactivity was detected
 */
public record RecordProlongedInactivityCommand(UUID personUnderCareId, Instant detectedAt) {
}
