package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.CreateCareRecipientProfileCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.CreateCareRecipientProfileResource;

/**
 * Assembler to convert a {@link CreateCareRecipientProfileResource}
 * to a {@link CreateCareRecipientProfileCommand}.
 */
public final class CreateCareRecipientProfileCommandFromResourceAssembler {

    private CreateCareRecipientProfileCommandFromResourceAssembler() {
    }

    public static CreateCareRecipientProfileCommand toCommandFromResource(
            CreateCareRecipientProfileResource resource) {

        return new CreateCareRecipientProfileCommand(
                resource.createdByUserId(),
                resource.firstName(),
                resource.lastName(),
                resource.birthDate(),
                resource.profileImageUrl());
    }
}