package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record RegisterCallLogRequestResource(
        UUID fragileCitizenId,
        String attemptedType,
        String reason
) {}
