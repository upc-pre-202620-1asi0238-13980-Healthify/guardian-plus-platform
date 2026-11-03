package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.CreateUserProfileCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.CreateUserProfileResource;

/**
 * Assembler to convert a {@link CreateUserProfileResource}
 * to a {@link CreateUserProfileCommand}.
 */
public final class CreateUserProfileCommandFromResourceAssembler {

    private CreateUserProfileCommandFromResourceAssembler() {
    }

    public static CreateUserProfileCommand toCommandFromResource(
            CreateUserProfileResource resource) {

        return new CreateUserProfileCommand(
                resource.userId(),
                resource.firstName(),
                resource.lastName(),
                resource.phoneNumber(),
                resource.profileImageUrl());
    }
}