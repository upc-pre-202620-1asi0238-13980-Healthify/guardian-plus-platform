package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UserProfileResource;

/**
 * Assembler that converts a {@link UserProfile}
 * aggregate into a {@link UserProfileResource}.
 */
public final class UserProfileResourceFromEntityAssembler {

    private UserProfileResourceFromEntityAssembler() {
    }

    public static UserProfileResource toResourceFromEntity(
            UserProfile profile) {

        return new UserProfileResource(
                profile.getId().value(),
                profile.getUserId().value(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhoneNumber(),
                profile.getProfileImageUrl(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}