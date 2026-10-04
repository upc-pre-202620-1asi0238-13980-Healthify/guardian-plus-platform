package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.acl;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.LocationPoint;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.acl.MobilityGeofencingContextFacade;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MobilityGeofencingContextFacadeImpl implements MobilityGeofencingContextFacade {

    // Simulación/Caché en memoria de la última ubicación conocida por paciente
    private final Map<UUID, LocationPoint> lastKnownLocations = new ConcurrentHashMap<>();

    public void updateLastKnownLocation(UUID careRecipientProfileId, LocationPoint location) {
        if (careRecipientProfileId != null && location != null) {
            lastKnownLocations.put(careRecipientProfileId, location);
        }
    }

    @Override
    public Optional<LocationPoint> getLastKnownLocation(UUID careRecipientProfileId) {
        return Optional.ofNullable(lastKnownLocations.get(careRecipientProfileId));
    }
}