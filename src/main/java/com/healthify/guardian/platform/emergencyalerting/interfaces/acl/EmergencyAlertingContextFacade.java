package com.healthify.guardian.platform.emergencyalerting.interfaces.acl;

import java.util.UUID;

/**
 * Public, read-only entry point other bounded contexts may call synchronously.
 */
public interface EmergencyAlertingContextFacade {

    /**
     * Tells whether a Fragile Citizen currently has any alert that is neither dismissed nor resolved.
     *
     * @param careRecipientProfileId the Fragile Citizen
     * @return true if at least one alert is active
     */
    boolean hasActiveAlerts(UUID careRecipientProfileId);
}
