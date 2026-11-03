package com.healthify.guardian.platform.profile.domain.model.commands;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.RelationshipType;

import java.util.UUID;

/**
 * Command to establish a relationship between a user and a person under care.
 *
 * @param userId                 the referenced IAM user
 * @param careRecipientProfileId the person under care
 * @param relationshipType       the type of care relationship
 */
public record EstablishCareRelationshipCommand(
        UUID userId,
        UUID careRecipientProfileId,
        RelationshipType relationshipType) {
}