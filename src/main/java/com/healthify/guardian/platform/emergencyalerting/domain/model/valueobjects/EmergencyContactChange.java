package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

/**
 * Kind of change applied to an emergency contact, carried by {@code EmergencyContactsChangedEvent}.
 */
public enum EmergencyContactChange {
    ADDED,
    DETAILS_UPDATED,
    REPRIORITIZED,
    ACTIVATED,
    DEACTIVATED
}
