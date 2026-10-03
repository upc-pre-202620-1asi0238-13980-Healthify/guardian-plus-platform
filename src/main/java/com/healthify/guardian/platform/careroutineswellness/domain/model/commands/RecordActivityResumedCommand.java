package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to record that a person under care has resumed physical activity after a
 * prolonged-inactivity episode.
 *
 * @param personUnderCareId the person whose activity monitor must transition
 * @param resumedAt          when activity resumed
 */
public record RecordActivityResumedCommand(UUID personUnderCareId, Instant resumedAt) {
}
