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
        var normal = vitalSignType.getNormalRange();
        var limits = vitalSignType.getPhysicalLimits();
        return new VitalSignTypeResource(
                vitalSignType.getId().value(),
                vitalSignType.getCode().value(),
                vitalSignType.getName(),
                vitalSignType.getUnit(),
                normal == null ? null : normal.minimum(),
                normal == null ? null : normal.maximum(),
                limits == null ? null : limits.minimum(),
                limits == null ? null : limits.maximum());
    }
}
