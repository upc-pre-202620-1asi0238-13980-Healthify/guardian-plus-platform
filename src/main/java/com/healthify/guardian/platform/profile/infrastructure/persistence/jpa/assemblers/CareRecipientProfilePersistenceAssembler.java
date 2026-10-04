package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.CareRecipientProfilePersistenceEntity;

/**
 * Static assembler between the {@link CareRecipientProfile} domain aggregate
 * and its persistence entity.
 */
public final class CareRecipientProfilePersistenceAssembler {

    private CareRecipientProfilePersistenceAssembler() {
    }

    public static CareRecipientProfile toDomainFromPersistence(
            CareRecipientProfilePersistenceEntity entity) {

        if (entity == null) return null;

        var profile = new CareRecipientProfile();

        profile.setId(new CareRecipientProfileId(entity.getId()));
        profile.setCreatedByUserId(entity.getCreatedByUserId());
        profile.setFirstName(entity.getFirstName());
        profile.setLastName(entity.getLastName());
        profile.setBirthDate(entity.getBirthDate());
        profile.setProfileImageUrl(entity.getProfileImageUrl());
        profile.setCreatedAt(entity.getCreatedAt().toInstant());
        profile.setUpdatedAt(entity.getUpdatedAt().toInstant());

        return profile;
    }

    public static CareRecipientProfilePersistenceEntity toPersistenceFromDomain(
            CareRecipientProfile profile) {

        if (profile == null) return null;

        var entity = new CareRecipientProfilePersistenceEntity();

        entity.setId(profile.getId().value());
        entity.setCreatedByUserId(profile.getCreatedByUserId());
        entity.setFirstName(profile.getFirstName());
        entity.setLastName(profile.getLastName());
        entity.setBirthDate(profile.getBirthDate());
        entity.setProfileImageUrl(profile.getProfileImageUrl());

        return entity;
    }
}