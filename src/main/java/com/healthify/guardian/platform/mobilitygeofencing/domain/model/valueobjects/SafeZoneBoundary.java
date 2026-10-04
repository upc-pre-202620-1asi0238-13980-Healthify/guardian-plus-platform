package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

public record SafeZoneBoundary(Coordinates center, Double radiusInMeters) {
    public SafeZoneBoundary {
        if (center == null) {
            throw new IllegalArgumentException("El centro de la geocerca no puede ser nulo.");
        }
        if (radiusInMeters == null || radiusInMeters <= 0) {
            throw new IllegalArgumentException("El radio debe ser mayor a 0 metros.");
        }
    }

    public boolean contains(Coordinates target) {
        if (target == null) return false;

        final int EARTH_RADIUS_METERS = 6371000;
        double lat1Rad = Math.toRadians(center.latitude());
        double lat2Rad = Math.toRadians(target.latitude());
        double deltaLat = Math.toRadians(target.latitude() - center.latitude());
        double deltaLon = Math.toRadians(target.longitude() - center.longitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (EARTH_RADIUS_METERS * c) <= radiusInMeters;
    }
}
