package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.VitalSignTypePersistenceEntity;

import java.math.BigDecimal;

/**
 * Static assembler between the {@link VitalSignType} aggregate and its persistence entity.
 */
public final class VitalSignTypePersistenceAssembler {

    private VitalSignTypePersistenceAssembler() {
    }

    public static VitalSignType toDomainFromPersistence(VitalSignTypePersistenceEntity entity) {
        if (entity == null) return null;
        var vitalSignType = new VitalSignType();
        vitalSignType.setId(new VitalSignTypeId(entity.getId()));
        vitalSignType.setCode(new VitalSignTypeCode(entity.getCode()));
        vitalSignType.setName(entity.getName());
        vitalSignType.setUnit(entity.getUnit());
        vitalSignType.setNormalRange(rangeOf(entity.getNormalMinimum(), entity.getNormalMaximum()));
        vitalSignType.setPhysicalLimits(rangeOf(entity.getPhysicalMinimum(), entity.getPhysicalMaximum()));
        return vitalSignType;
    }

    public static VitalSignTypePersistenceEntity toPersistenceFromDomain(VitalSignType vitalSignType) {
        if (vitalSignType == null) return null;
        var entity = new VitalSignTypePersistenceEntity();
        entity.setId(vitalSignType.getId().value());
        entity.setCode(vitalSignType.getCode().value());
        entity.setName(vitalSignType.getName());
        entity.setUnit(vitalSignType.getUnit());
        if (vitalSignType.getNormalRange() != null) {
            entity.setNormalMinimum(vitalSignType.getNormalRange().minimum());
            entity.setNormalMaximum(vitalSignType.getNormalRange().maximum());
        }
        if (vitalSignType.getPhysicalLimits() != null) {
            entity.setPhysicalMinimum(vitalSignType.getPhysicalLimits().minimum());
            entity.setPhysicalMaximum(vitalSignType.getPhysicalLimits().maximum());
        }
        return entity;
    }

    private static VitalSignRange rangeOf(BigDecimal minimum, BigDecimal maximum) {
        return minimum == null || maximum == null ? null : new VitalSignRange(minimum, maximum);
    }
}
