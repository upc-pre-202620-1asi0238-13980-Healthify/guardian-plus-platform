package com.healthify.guardian.platform.mobilitygeofencing.domain.model.events;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;
import java.time.Instant;

public record SafeZoneViolationDetectedEvent(
        SafeZoneId safeZoneId,
        FragileCitizenId fragileCitizenId,
        Location location,
        Instant detectedAt
) {}
