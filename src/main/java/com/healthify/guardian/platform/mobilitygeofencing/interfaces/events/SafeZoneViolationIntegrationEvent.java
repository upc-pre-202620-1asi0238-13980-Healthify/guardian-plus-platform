package com.healthify.guardian.platform.mobilitygeofencing.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record SafeZoneViolationIntegrationEvent(
        UUID careRecipientProfileId,
        Double lastLatitude,
        Double lastLongitude,
        Instant occurredAt
) {}
