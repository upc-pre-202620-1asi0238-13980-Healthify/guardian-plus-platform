package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for configuring how prolonged inactivity is detected.
 *
 * @param enabled          whether prolonged inactivity raises an alert ("Detección activa")
 * @param thresholdMinutes minutes without movement after which an alert is raised (30 to 240)
 */
public record ConfigureInactivityDetectionResource(

        @NotNull(message = "{activity-monitor.enabled.blank}")
        Boolean enabled,

        @NotNull(message = "{activity-monitor.threshold-minutes.invalid}")
        Integer thresholdMinutes
) {
}
