package com.healthify.guardian.platform.profile.domain.model.events;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipId;

import java.time.Instant;

/**
 * Raised when a care relationship is ended.
 */
public record CareRelationshipEndedEvent(
        CareRelationshipId careRelationshipId,
        Instant endedAt) {

    public static CareRelationshipEndedEvent from(CareRelationship relationship) {
        return new CareRelationshipEndedEvent(
                relationship.getId(),
                relationship.getEndedAt());
    }
}