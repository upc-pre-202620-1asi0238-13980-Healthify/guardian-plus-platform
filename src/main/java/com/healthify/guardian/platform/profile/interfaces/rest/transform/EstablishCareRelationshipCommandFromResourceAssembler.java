package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.EstablishCareRelationshipCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.EstablishCareRelationshipResource;

/**
 * Assembler to convert an {@link EstablishCareRelationshipResource}
 * to an {@link EstablishCareRelationshipCommand}.
 */
public final class EstablishCareRelationshipCommandFromResourceAssembler {

    private EstablishCareRelationshipCommandFromResourceAssembler() {
    }

    public static EstablishCareRelationshipCommand toCommandFromResource(
            EstablishCareRelationshipResource resource) {

        return new EstablishCareRelationshipCommand(
                resource.userId(),
                resource.careRecipientProfileId(),
                resource.relationshipType());
    }
}