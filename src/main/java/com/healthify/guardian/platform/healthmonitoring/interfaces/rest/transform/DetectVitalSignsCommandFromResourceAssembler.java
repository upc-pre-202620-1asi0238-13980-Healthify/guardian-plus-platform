package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.DetectVitalSignsResource;

import java.time.Instant;

/**
 * Assembler converting a {@link DetectVitalSignsResource} into a {@link DetectVitalSignsCommand}.
 * The reception time is the server time at which the request arrived.
 */
public final class DetectVitalSignsCommandFromResourceAssembler {

    private DetectVitalSignsCommandFromResourceAssembler() {
    }

    public static DetectVitalSignsCommand toCommandFromResource(DetectVitalSignsResource resource, Instant receivedAt) {
        return new DetectVitalSignsCommand(
                resource.wearableDeviceId(),
                resource.careRecipientProfileId(),
                resource.vitalSignType(),
                resource.value(),
                resource.measuredAt(),
                receivedAt);
    }
}
