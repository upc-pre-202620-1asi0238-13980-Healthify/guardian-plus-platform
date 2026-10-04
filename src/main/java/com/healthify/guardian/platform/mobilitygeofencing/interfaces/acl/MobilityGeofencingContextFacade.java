package com.healthify.guardian.platform.mobilitygeofencing.interfaces.acl;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.LocationPoint;

import java.util.Optional;
import java.util.UUID;

public interface MobilityGeofencingContextFacade {
    /**
     * Provides a patient's last known location for Emergency & Alerting[cite: 1].
     */
    Optional<LocationPoint> getLastKnownLocation(UUID careRecipientProfileId);
}
