package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignTypeResource;

/**
 * Assembler converting a {@link VitalSignType} aggregate into a {@link VitalSignTypeResource}.
 */
public final class VitalSignTypeResourceFromEntityAssembler {

    private VitalSignTypeResourceFromEntityAssembler() {
    }

    public static VitalSignTypeResource toResourceFromEntity(VitalSignType vitalSignType) {
        return new VitalSignTypeResource(
                vitalSignType.getId().value(),
                vitalSignType.getCode().value(),
                vitalSignType.getName(),
                vitalSignType.getUnit());
    }
}
