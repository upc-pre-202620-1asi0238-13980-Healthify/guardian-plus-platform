package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing a care relationship.
 *
 * @param id                     the relationship identifier
 * @param userId                 the Guardian+ user
 * @param careRecipientProfileId the person under care
 * @param relationshipType       the relationship type
 * @param status                 the relationship lifecycle status
 * @param startedAt              when the relationship was established
 * @param endedAt                when the relationship ended, if applicable
 */
public record CareRelationshipResource(
        UUID id,
        UUID userId,
        UUID careRecipientProfileId,
        String relationshipType,
        String status,
        Instant startedAt,
        Instant endedAt
) {
}