package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignResource;

/**
 * Assembler converting a {@link VitalSign} aggregate into a {@link VitalSignResource}.
 */
public final class VitalSignResourceFromEntityAssembler {

    private VitalSignResourceFromEntityAssembler() {
    }

    public static VitalSignResource toResourceFromEntity(VitalSign vitalSign) {
        return new VitalSignResource(
                vitalSign.getId().value(),
                vitalSign.getWearableDeviceId().value(),
                vitalSign.getCareRecipientProfileId().value(),
                vitalSign.getVitalSignType().code(),
                vitalSign.getValue().value(),
                vitalSign.getMeasuredAt(),
                vitalSign.getReceivedAt(),
                vitalSign.getEmittedAt());
    }
}
