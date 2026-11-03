package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.transform;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.SafeZoneResource;

public class SafeZoneResourceAssembler {

    public static SafeZoneResource toResource(SafeZone safeZone) {
        return new SafeZoneResource(
                safeZone.getId().value(),
                safeZone.getFragileCitizenId().value(),
                safeZone.getName(),
                safeZone.getBoundary().center().latitude(),
                safeZone.getBoundary().center().longitude(),
                safeZone.getBoundary().radiusInMeters(),
                safeZone.getStatus().name()
        );
    }
}