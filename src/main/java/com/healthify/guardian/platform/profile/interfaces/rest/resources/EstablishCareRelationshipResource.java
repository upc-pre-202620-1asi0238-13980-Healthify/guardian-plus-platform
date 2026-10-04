package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.RelationshipType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload to establish a care relationship.
 *
 * @param userId                 the Guardian+ user participating in the relationship
 * @param careRecipientProfileId the person under care
 * @param relationshipType       the relationship type
 */
public record EstablishCareRelationshipResource(

        @NotNull(message = "{care-relationship.user-id.blank}")
        UUID userId,

        @NotNull(message = "{care-relationship.care-recipient-profile-id.blank}")
        UUID careRecipientProfileId,

        @NotNull(message = "{care-relationship.relationship-type.blank}")
        RelationshipType relationshipType
) {
}