package com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands;

import java.util.UUID;

public record ProcessTelemetryCommand(
        UUID careRecipientProfileId,
        Double latitude,
        Double longitude,
        Float accuracy
) {}
