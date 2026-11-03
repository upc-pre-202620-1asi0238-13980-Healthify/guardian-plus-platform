package com.healthify.guardian.platform.profile.domain.model.events;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.RelationshipType;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

import java.time.Instant;

/**
 * Raised when a care relationship is established.
 */
public record CareRelationshipEstablishedEvent(
        CareRelationshipId careRelationshipId,
        UserId userId,
        CareRecipientProfileId careRecipientProfileId,
        RelationshipType relationshipType,
        Instant startedAt) {

    public static CareRelationshipEstablishedEvent from(CareRelationship relationship) {
        return new CareRelationshipEstablishedEvent(
                relationship.getId(),
                relationship.getUserId(),
                relationship.getCareRecipientProfileId(),
                relationship.getRelationshipType(),
                relationship.getStartedAt());
    }
}