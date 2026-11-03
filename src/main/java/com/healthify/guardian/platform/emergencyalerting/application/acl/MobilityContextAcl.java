package com.healthify.guardian.platform.emergencyalerting.application.acl;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

import java.time.Instant;
import java.util.Optional;

/**
 * Anti-corruption layer towards the {@code Mobility & Geofencing} bounded context. The location is
 * only included in notification contents and never persisted by this context.
 */
public interface MobilityContextAcl {

    /**
     * Retrieves the Fragile Citizen's last known location.
     *
     * @param careRecipientProfileId the Fragile Citizen
     * @return their last known location, if any
     */
    Optional<LastKnownLocation> findLastKnownLocation(CareRecipientProfileId careRecipientProfileId);

    /**
     * Last known position of a Fragile Citizen.
     *
     * @param latitude   latitude in decimal degrees
     * @param longitude  longitude in decimal degrees
     * @param recordedAt when the position was recorded
     */
    record LastKnownLocation(Double latitude, Double longitude, Instant recordedAt) {
    }
}
