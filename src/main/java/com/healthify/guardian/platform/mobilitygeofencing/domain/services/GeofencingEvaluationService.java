package com.healthify.guardian.platform.mobilitygeofencing.domain.services;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneViolationEvent;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.LocationPoint;

import java.util.List;
import java.util.Optional;

public class GeofencingEvaluationService {

    /**
     * Evalúa si una nueva ubicación respeta al menos una zona segura activa.
     * @return Optional con el evento de violación si el usuario está fuera de todas sus zonas.
     */
    public Optional<SafeZoneViolationEvent> evaluateLocation(
            CareRecipientProfileId profileId,
            LocationPoint currentLocation,
            List<SafeZone> activeSafeZones) {

        if (activeSafeZones == null || activeSafeZones.isEmpty()) {
            return Optional.empty();
        }

        boolean isInsideAnyZone = activeSafeZones.stream()
                .filter(SafeZone::isActive)
                .anyMatch(zone -> zone.contains(currentLocation));

        if (!isInsideAnyZone) {
            return Optional.of(new SafeZoneViolationEvent(profileId, currentLocation));
        }

        return Optional.empty();
    }
}