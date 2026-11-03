package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

public record UpdateSafeZoneResource(
        String name,
        Double centerLatitude,
        Double centerLongitude,
        Double radiusInMeters
) {}
