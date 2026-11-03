package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.UpdateCareRecipientProfileCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateCareRecipientProfileResource;

import java.util.UUID;

/**
 * Assembler to convert an {@link UpdateCareRecipientProfileResource}
 * to an {@link UpdateCareRecipientProfileCommand}.
 */
public final class UpdateCareRecipientProfileCommandFromResourceAssembler {

    private UpdateCareRecipientProfileCommandFromResourceAssembler() {
    }

    public static UpdateCareRecipientProfileCommand toCommandFromResource(
            UUID careRecipientProfileId,
            UpdateCareRecipientProfileResource resource) {

        return new UpdateCareRecipientProfileCommand(
                careRecipientProfileId,
                resource.firstName(),
                resource.lastName(),
                resource.birthDate());
    }
}