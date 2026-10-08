package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.transform;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.LocationTracking;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Location;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.CurrentLocationResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.LocationHistoryResource;

public class LocationResourceAssembler {

    public static CurrentLocationResource toCurrentResource(LocationTracking tracking) {
        return new CurrentLocationResource(
                tracking.getFragileCitizenId().value(),
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().coordinates().latitude() : null,
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().coordinates().longitude() : null,
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().accuracyInMeters() : null,
                tracking.getCurrentStatus() != null ? tracking.getCurrentStatus().name() : null,
                tracking.getLastUpdatedAt()
        );
    }

    public static LocationHistoryResource toHistoryResource(Location location) {
        return new LocationHistoryResource(
                location.coordinates().latitude(),
                location.coordinates().longitude(),
                location.accuracyInMeters(),
                "RECORDED",
                location.recordedAt()
        );
    }
}