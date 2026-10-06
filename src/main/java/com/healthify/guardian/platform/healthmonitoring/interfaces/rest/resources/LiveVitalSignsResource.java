package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Live view of the vital signs of a care recipient.
 */
public record LiveVitalSignsResource(
        UUID careRecipientProfileId,
        Instant retrievedAt,
        List<LiveVitalSignResource> vitalSigns
) {
}
