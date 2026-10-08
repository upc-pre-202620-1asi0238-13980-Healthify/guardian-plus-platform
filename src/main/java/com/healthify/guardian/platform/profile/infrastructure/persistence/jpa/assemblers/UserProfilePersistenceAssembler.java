package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities.UserProfilePersistenceEntity;

/**
 * Static assembler between the {@link UserProfile} domain aggregate
 * and its persistence entity.
 */
public final class UserProfilePersistenceAssembler {

    private UserProfilePersistenceAssembler() {
    }

    public static UserProfile toDomainFromPersistence(UserProfilePersistenceEntity entity) {
        if (entity == null) return null;

        var profile = new UserProfile();

        profile.setId(new UserProfileId(entity.getId()));
        profile.setUserId(entity.getUserId());
        profile.setFirstName(entity.getFirstName());
        profile.setLastName(entity.getLastName());
        profile.setPhoneNumber(entity.getPhoneNumber());
        profile.setProfileImageUrl(entity.getProfileImageUrl());
        profile.setCreatedAt(entity.getCreatedAt().toInstant());
        profile.setUpdatedAt(entity.getUpdatedAt().toInstant());

        return profile;
    }

    public static UserProfilePersistenceEntity toPersistenceFromDomain(UserProfile profile) {
        if (profile == null) return null;

        var entity = new UserProfilePersistenceEntity();

        entity.setId(profile.getId().value());
        entity.setUserId(profile.getUserId());
        entity.setFirstName(profile.getFirstName());
        entity.setLastName(profile.getLastName());
        entity.setPhoneNumber(profile.getPhoneNumber());
        entity.setProfileImageUrl(profile.getProfileImageUrl());

        return entity;
    }
}