package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.UpdateCareRecipientProfileImageCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateProfileImageResource;

import java.util.UUID;

/**
 * Assembler to convert an {@link UpdateProfileImageResource}
 * to an {@link UpdateCareRecipientProfileImageCommand}.
 */
public final class UpdateCareRecipientProfileImageCommandFromResourceAssembler {

    private UpdateCareRecipientProfileImageCommandFromResourceAssembler() {
    }

    public static UpdateCareRecipientProfileImageCommand toCommandFromResource(
            UUID careRecipientProfileId,
            UpdateProfileImageResource resource) {

        return new UpdateCareRecipientProfileImageCommand(
                careRecipientProfileId,
                resource.profileImageUrl());
    }
}