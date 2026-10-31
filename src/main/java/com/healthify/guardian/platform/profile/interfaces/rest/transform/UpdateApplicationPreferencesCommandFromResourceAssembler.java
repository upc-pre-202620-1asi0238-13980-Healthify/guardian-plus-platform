package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.UpdateApplicationPreferencesCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateApplicationPreferencesResource;

import java.util.UUID;

/**
 * Assembler to convert an {@link UpdateApplicationPreferencesResource}
 * to an {@link UpdateApplicationPreferencesCommand}.
 */
public final class UpdateApplicationPreferencesCommandFromResourceAssembler {

    private UpdateApplicationPreferencesCommandFromResourceAssembler() {
    }

    public static UpdateApplicationPreferencesCommand toCommandFromResource(
            UUID userId,
            UpdateApplicationPreferencesResource resource) {

        return new UpdateApplicationPreferencesCommand(
                userId,
                resource.notificationsEnabled());
    }
}