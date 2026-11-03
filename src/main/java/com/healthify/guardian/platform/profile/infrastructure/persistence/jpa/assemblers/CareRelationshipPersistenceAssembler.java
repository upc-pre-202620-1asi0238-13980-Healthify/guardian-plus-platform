package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.CareRelationshipPersistenceEntity;

/**
 * Static assembler between the {@link CareRelationship} domain aggregate
 * and its persistence entity.
 */
public final class CareRelationshipPersistenceAssembler {

    private CareRelationshipPersistenceAssembler() {
    }

    public static CareRelationship toDomainFromPersistence(
            CareRelationshipPersistenceEntity entity) {

        if (entity == null) return null;

        var relationship = new CareRelationship();

        relationship.setId(new CareRelationshipId(entity.getId()));
        relationship.setUserId(entity.getUserId());
        relationship.setCareRecipientProfileId(entity.getCareRecipientProfileId());
        relationship.setRelationshipType(entity.getRelationshipType());
        relationship.setStatus(entity.getStatus());
        relationship.setStartedAt(entity.getStartedAt());
        relationship.setEndedAt(entity.getEndedAt());

        return relationship;
    }

    public static CareRelationshipPersistenceEntity toPersistenceFromDomain(
            CareRelationship relationship) {

        if (relationship == null) return null;

        var entity = new CareRelationshipPersistenceEntity();

        entity.setId(relationship.getId().value());
        entity.setUserId(relationship.getUserId());
        entity.setCareRecipientProfileId(
                relationship.getCareRecipientProfileId());
        entity.setRelationshipType(relationship.getRelationshipType());
        entity.setStatus(relationship.getStatus());
        entity.setStartedAt(relationship.getStartedAt());
        entity.setEndedAt(relationship.getEndedAt());

        return entity;
    }
}