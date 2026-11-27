package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record StartCallRequestResource(
        UUID fragileCitizenId,
        String requestedType
) {}
