package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignTypeResource;

/**
 * Assembler converting a {@link VitalSignType} into a {@link VitalSignTypeResource}.
 */
public final class VitalSignTypeResourceFromEntityAssembler {

    private VitalSignTypeResourceFromEntityAssembler() {
    }

    public static VitalSignTypeResource toResourceFromEntity(VitalSignType vitalSignType) {
        return new VitalSignTypeResource(
                vitalSignType.code(),
                vitalSignType.displayName(),
                vitalSignType.unit(),
                vitalSignType.normalRange().minimum(),
                vitalSignType.normalRange().maximum(),
                vitalSignType.physicalLimits().minimum(),
                vitalSignType.physicalLimits().maximum());
    }
}
