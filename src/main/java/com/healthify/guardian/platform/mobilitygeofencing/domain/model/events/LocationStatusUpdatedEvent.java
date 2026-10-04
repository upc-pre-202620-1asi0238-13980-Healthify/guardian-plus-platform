package com.healthify.guardian.platform.mobilitygeofencing.domain.model.events;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;
import java.time.Instant;

public record LocationStatusUpdatedEvent(FragileCitizenId fragileCitizenId, LocationStatus status, Instant occurredAt) {}
