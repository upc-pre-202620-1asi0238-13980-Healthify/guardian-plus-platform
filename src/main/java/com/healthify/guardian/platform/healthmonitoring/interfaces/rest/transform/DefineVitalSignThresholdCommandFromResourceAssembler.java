package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.DefineVitalSignThresholdResource;

/**
 * Assembler converting a {@link DefineVitalSignThresholdResource} into a {@link DefineVitalSignThresholdCommand}.
 */
public final class DefineVitalSignThresholdCommandFromResourceAssembler {

    private DefineVitalSignThresholdCommandFromResourceAssembler() {
    }

    public static DefineVitalSignThresholdCommand toCommandFromResource(DefineVitalSignThresholdResource resource) {
        return new DefineVitalSignThresholdCommand(
                resource.careRecipientProfileId(),
                resource.vitalSignTypeId(),
                resource.minimumValue(),
                resource.maximumValue(),
                resource.requiredConsecutiveHits());
    }
}
