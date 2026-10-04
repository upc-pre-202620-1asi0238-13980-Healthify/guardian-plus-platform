package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.UpdateUserProfileImageCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateProfileImageResource;

import java.util.UUID;

/**
 * Assembler to convert an {@link UpdateProfileImageResource}
 * to an {@link UpdateUserProfileImageCommand}.
 */
public final class UpdateUserProfileImageCommandFromResourceAssembler {

    private UpdateUserProfileImageCommandFromResourceAssembler() {
    }

    public static UpdateUserProfileImageCommand toCommandFromResource(
            UUID userProfileId,
            UpdateProfileImageResource resource) {

        return new UpdateUserProfileImageCommand(
                userProfileId,
                resource.profileImageUrl());
    }
}