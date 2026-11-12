package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record DeviceCapabilitiesResource(
        UUID fragileCitizenId,
        String wristbandModel,
        boolean hasCamera,
        boolean hasScreen,
        boolean hasMicrophone,
        boolean hasSpeaker,
        boolean supportsHapticAlerts,
        boolean supportsTelemetry,
        String citizenPhone,
        String caregiverPhone
) {}
