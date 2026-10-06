package com.healthify.guardian.platform.profile.domain.services;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.RelationshipType;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.repositories.CareRelationshipRepository;

/**
 * Domain service that evaluates whether a care relationship can be established.
 */
public class CareRelationshipPolicy {

    private static final String INVALID_RELATIONSHIP_TYPE_MESSAGE_KEY =
            "care-relationship.type.invalid";

    private final CareRelationshipRepository careRelationshipRepository;

    public CareRelationshipPolicy(
            CareRelationshipRepository careRelationshipRepository) {
        this.careRelationshipRepository = careRelationshipRepository;
    }

    /**
     * Returns whether the same user does not already have an active relationship
     * with the same person under care.
     */
    public boolean canEstablishRelationship(
            UserId userId,
            CareRecipientProfileId careRecipientProfileId) {

        if (userId == null || careRecipientProfileId == null) {
            return false;
        }

        return careRelationshipRepository
                .findActiveByUserId(userId)
                .stream()
                .noneMatch(relationship ->
                        careRecipientProfileId.equals(
                                relationship.getCareRecipientProfileId()));
    }

    /**
     * Validates that the relationship type is supported by the domain.
     */
    public void validateRelationshipType(RelationshipType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    INVALID_RELATIONSHIP_TYPE_MESSAGE_KEY);
        }
    }
}