package com.healthify.guardian.platform.mobilitygeofencing.domain.services;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;

public class GeofenceEvaluationService {

    public LocationStatus evaluate(Location location, SafeZoneBoundary boundary) {
        return isInside(location, boundary) ? LocationStatus.WITHIN_SAFE_ZONE : LocationStatus.OUTSIDE_SAFE_ZONE;
    }

    public boolean isInside(Location location, SafeZoneBoundary boundary) {
        if (location == null || boundary == null) return false;
        return boundary.contains(location.coordinates());
    }
}
