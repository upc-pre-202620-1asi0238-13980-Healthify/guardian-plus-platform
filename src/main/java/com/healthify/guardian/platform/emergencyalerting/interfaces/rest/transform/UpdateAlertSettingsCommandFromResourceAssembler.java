package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.UpdateAlertSettingsCommand;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.UpdateAlertSettingsResource;

import java.util.UUID;

/**
 * Assembler to convert an {@link UpdateAlertSettingsResource} to an {@link UpdateAlertSettingsCommand}.
 */
public final class UpdateAlertSettingsCommandFromResourceAssembler {

    private UpdateAlertSettingsCommandFromResourceAssembler() {
    }

    public static UpdateAlertSettingsCommand toCommandFromResource(
            UUID careRecipientProfileId, UpdateAlertSettingsResource resource) {
        return new UpdateAlertSettingsCommand(
                careRecipientProfileId,
                resource.primaryAckTimeoutSec(),
                resource.escalationEnabled(),
                resource.broadcastCriticalImmediately());
    }
}
