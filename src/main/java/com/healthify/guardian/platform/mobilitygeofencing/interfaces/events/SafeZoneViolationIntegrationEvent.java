package com.healthify.guardian.platform.mobilitygeofencing.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published by {@code Mobility & Geofencing} when a location received from the wearable
 * falls outside the person's active safe zone.
 *
 * <p>This is the <em>published language</em> of this bounded context towards {@code Emergency & Alerting},
 * which consumes it to trigger a {@code SAFE_ZONE_VIOLATION} alert. It is published for every location
 * received outside the zone, not only for the first one.</p>
 *
 * @param careRecipientProfileId the Fragile Citizen who left the zone
 * @param safeZoneId             the safe zone that was left
 * @param lastLatitude           latitude of the location received outside the zone
 * @param lastLongitude          longitude of the location received outside the zone
 * @param occurredAt             when the violation was detected
 */
public record SafeZoneViolationIntegrationEvent(
        UUID careRecipientProfileId,
        UUID safeZoneId,
        Double lastLatitude,
        Double lastLongitude,
        Instant occurredAt
) {}
