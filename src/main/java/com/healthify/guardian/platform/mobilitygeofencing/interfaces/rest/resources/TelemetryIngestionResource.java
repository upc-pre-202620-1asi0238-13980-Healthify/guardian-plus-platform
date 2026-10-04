package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record TelemetryIngestionResource(
        UUID careRecipientProfileId,
        Double latitude,
        Double longitude,
        Float accuracy
) {}
