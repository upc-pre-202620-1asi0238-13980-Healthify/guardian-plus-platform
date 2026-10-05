package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignThresholdResource;

/**
 * Assembler converting a {@link VitalSignThreshold} aggregate into a {@link VitalSignThresholdResource}.
 */
public final class VitalSignThresholdResourceFromEntityAssembler {

    private VitalSignThresholdResourceFromEntityAssembler() {
    }

    public static VitalSignThresholdResource toResourceFromEntity(VitalSignThreshold threshold) {
        return new VitalSignThresholdResource(
                threshold.getId().value(),
                threshold.getCareRecipientProfileId().value(),
                threshold.getVitalSignTypeId().value(),
                threshold.getMinimumValue(),
                threshold.getMaximumValue(),
                threshold.getRequiredConsecutiveHits(),
                threshold.getActive(),
                threshold.getUpdatedAt());
    }
}
