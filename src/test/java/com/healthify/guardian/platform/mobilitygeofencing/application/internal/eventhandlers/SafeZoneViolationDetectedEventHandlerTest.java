package com.healthify.guardian.platform.mobilitygeofencing.application.internal.eventhandlers;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneViolationDetectedEvent;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Coordinates;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Location;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.SafeZoneViolationIntegrationEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of {@link SafeZoneViolationDetectedEventHandler}.
 */
class SafeZoneViolationDetectedEventHandlerTest {

    private final List<Object> published = new ArrayList<>();
    private final SafeZoneViolationDetectedEventHandler handler = new SafeZoneViolationDetectedEventHandler(published::add);

    @Test
    void theViolationIsRepublishedForEmergencyAndAlerting() {
        var person = UUID.randomUUID();
        var zone = UUID.randomUUID();
        var detectedAt = Instant.parse("2026-10-22T15:00:00Z");

        handler.on(new SafeZoneViolationDetectedEvent(
                new SafeZoneId(zone),
                new FragileCitizenId(person),
                new Location(new Coordinates(-12.05, -77.04), detectedAt.minusSeconds(2), 8.0),
                detectedAt));

        assertThat(published).containsExactly(
                new SafeZoneViolationIntegrationEvent(person, zone, -12.05, -77.04, detectedAt));
    }
}
