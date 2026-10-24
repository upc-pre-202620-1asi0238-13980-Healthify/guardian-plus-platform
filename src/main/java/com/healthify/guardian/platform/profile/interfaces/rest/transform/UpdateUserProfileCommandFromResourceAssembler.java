package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.UpdateUserProfileCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateUserProfileResource;

import java.util.UUID;

/**
 * Assembler to convert an {@link UpdateUserProfileResource}
 * to an {@link UpdateUserProfileCommand}.
 */
public final class UpdateUserProfileCommandFromResourceAssembler {

    private UpdateUserProfileCommandFromResourceAssembler() {
    }

    public static UpdateUserProfileCommand toCommandFromResource(
            UUID userProfileId,
            UpdateUserProfileResource resource) {

        return new UpdateUserProfileCommand(
                userProfileId,
                resource.firstName(),
                resource.lastName());
    }
}