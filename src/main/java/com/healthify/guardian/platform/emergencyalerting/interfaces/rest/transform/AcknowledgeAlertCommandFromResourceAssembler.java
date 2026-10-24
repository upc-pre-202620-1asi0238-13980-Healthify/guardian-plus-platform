package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.AcknowledgeAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AcknowledgeAlertResource;

import java.util.UUID;

/**
 * Assembler to convert an {@link AcknowledgeAlertResource} to an {@link AcknowledgeAlertCommand}.
 */
public final class AcknowledgeAlertCommandFromResourceAssembler {

    private AcknowledgeAlertCommandFromResourceAssembler() {
    }

    public static AcknowledgeAlertCommand toCommandFromResource(UUID alertId, AcknowledgeAlertResource resource) {
        return new AcknowledgeAlertCommand(alertId, resource.userId());
    }
}
