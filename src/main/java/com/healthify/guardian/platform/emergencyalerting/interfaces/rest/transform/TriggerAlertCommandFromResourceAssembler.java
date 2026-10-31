package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.TriggerAlertResource;

import java.time.Instant;

/**
 * Assembler to convert a {@link TriggerAlertResource} to a {@link TriggerAlertCommand}.
 */
public final class TriggerAlertCommandFromResourceAssembler {

    private TriggerAlertCommandFromResourceAssembler() {
    }

    /**
     * @param receivedAt when the request reached the platform, used as the triggering time so that a
     *                   device with a skewed clock cannot shorten or stretch the fall confirmation window
     */
    public static TriggerAlertCommand toCommandFromResource(TriggerAlertResource resource, Instant receivedAt) {
        return new TriggerAlertCommand(
                resource.careRecipientProfileId(), resource.sourceType(), resource.sourceReferenceId(), receivedAt);
    }
}
