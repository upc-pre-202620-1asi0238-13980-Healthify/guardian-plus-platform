package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.CareRelationshipResource;

/**
 * Assembler that converts a {@link CareRelationship}
 * aggregate into a {@link CareRelationshipResource}.
 */
public final class CareRelationshipResourceFromEntityAssembler {

    private CareRelationshipResourceFromEntityAssembler() {
    }

    public static CareRelationshipResource toResourceFromEntity(
            CareRelationship relationship) {

        return new CareRelationshipResource(
                relationship.getId().value(),
                relationship.getUserId().value(),
                relationship.getCareRecipientProfileId().value(),
                relationship.getRelationshipType().name(),
                relationship.getStatus().name(),
                relationship.getStartedAt(),
                relationship.getEndedAt());
    }
}