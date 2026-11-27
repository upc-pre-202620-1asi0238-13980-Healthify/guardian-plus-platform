package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record HapticAlertRequestResource(
        UUID fragileCitizenId,
        String message
) {}
