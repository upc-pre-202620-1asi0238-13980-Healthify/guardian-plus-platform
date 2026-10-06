package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.UpdateContactInformationCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateContactInformationResource;

import java.util.UUID;

/**
 * Assembler to convert an {@link UpdateContactInformationResource}
 * to an {@link UpdateContactInformationCommand}.
 */
public final class UpdateContactInformationCommandFromResourceAssembler {

    private UpdateContactInformationCommandFromResourceAssembler() {
    }

    public static UpdateContactInformationCommand toCommandFromResource(
            UUID userProfileId,
            UpdateContactInformationResource resource) {

        return new UpdateContactInformationCommand(
                userProfileId,
                resource.phoneNumber());
    }
}