package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

/**
 * Immutable configuration of the prolonged-inactivity watch for a person under care.
 *
 * @param enabled          whether prolonged inactivity raises an alert at all
 * @param thresholdMinutes minutes without movement, during the watch hours, after which an alert is raised
 */
public record InactivityDetectionSettings(boolean enabled, Integer thresholdMinutes) {

    /** Business rule from the report: alert after 60 minutes without movement. */
    public static final int DEFAULT_THRESHOLD_MINUTES = 60;
    private static final int MIN_THRESHOLD_MINUTES = 30;
    private static final int MAX_THRESHOLD_MINUTES = 240;
    private static final String THRESHOLD_INVALID_MESSAGE_KEY = "activity-monitor.threshold-minutes.invalid";

    public InactivityDetectionSettings {
        if (thresholdMinutes == null
                || thresholdMinutes < MIN_THRESHOLD_MINUTES || thresholdMinutes > MAX_THRESHOLD_MINUTES) {
            throw new IllegalArgumentException(THRESHOLD_INVALID_MESSAGE_KEY);
        }
    }

    /** Settings every new activity monitor starts with: enabled, with the 60-minute threshold. */
    public static InactivityDetectionSettings defaults() {
        return new InactivityDetectionSettings(true, DEFAULT_THRESHOLD_MINUTES);
    }
}
