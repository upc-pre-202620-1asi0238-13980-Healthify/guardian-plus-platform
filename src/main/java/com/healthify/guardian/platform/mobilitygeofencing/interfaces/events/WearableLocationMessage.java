package com.healthify.guardian.platform.mobilitygeofencing.interfaces.events;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire format of the location published by the wearable (or the IoT simulator) on
 * {@code guardian/location/<deviceId>}. The wearable names the person by its care recipient profile, which is
 * the Fragile Citizen of this context; the rest of its fields (speed, distance from home...) are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WearableLocationMessage(
        @JsonAlias("careRecipientProfileId") UUID fragileCitizenId,
        Double latitude,
        Double longitude,
        Double accuracyInMeters,
        Instant recordedAt
) {}
