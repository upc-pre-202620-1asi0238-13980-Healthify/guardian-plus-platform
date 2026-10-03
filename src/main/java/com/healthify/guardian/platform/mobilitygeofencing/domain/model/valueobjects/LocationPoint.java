package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.util.Objects;

public record LocationPoint(Double latitude, Double longitude, Float accuracy) {

    public LocationPoint {
        if (latitude == null || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("The latitude must be between -90 and 90 degrees..");
        }
        if (longitude == null || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("The longitude must be between -180 and 180 degrees.s.");
        }
    }

    public LocationPoint(Double latitude, Double longitude) {
        this(latitude, longitude, 0.0f);
    }

    /**
     * Calculate the distance in meters between this point and another using the Haversine formula.
     */
    public double distanceToInMeters(LocationPoint other) {
        if (other == null) {
            throw new IllegalArgumentException("The destination point cannot be null.");
        }

        final int EARTH_RADIUS_METERS = 6371000;

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude());
        double deltaLat = Math.toRadians(other.latitude() - this.latitude);
        double deltaLon = Math.toRadians(other.longitude() - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}