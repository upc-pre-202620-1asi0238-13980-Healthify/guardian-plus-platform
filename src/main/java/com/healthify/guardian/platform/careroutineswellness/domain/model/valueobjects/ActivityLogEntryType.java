package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

/**
 * Kind of noteworthy activity fact kept in the person under care's recent activity log.
 */
public enum ActivityLogEntryType {
    /** Movement after a long still period: the inactivity counter was reset without raising an alarm. */
    MOVEMENT_DETECTED,
    /** A sustained walk finished. */
    WALK_DETECTED,
    /** The inactivity counter reached the configured threshold during the watch hours. */
    PROLONGED_INACTIVITY_DETECTED
}
