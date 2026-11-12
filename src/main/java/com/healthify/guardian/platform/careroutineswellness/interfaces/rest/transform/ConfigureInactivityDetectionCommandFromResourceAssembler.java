package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfigureInactivityDetectionCommand;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ConfigureInactivityDetectionResource;

import java.util.UUID;

/**
 * Assembler to convert a {@link ConfigureInactivityDetectionResource} to a
 * {@link ConfigureInactivityDetectionCommand}.
 */
public final class ConfigureInactivityDetectionCommandFromResourceAssembler {

    private ConfigureInactivityDetectionCommandFromResourceAssembler() {
    }

    public static ConfigureInactivityDetectionCommand toCommandFromResource(
            UUID personUnderCareId, ConfigureInactivityDetectionResource resource) {
        return new ConfigureInactivityDetectionCommand(personUnderCareId, resource.enabled(), resource.thresholdMinutes());
    }
}
