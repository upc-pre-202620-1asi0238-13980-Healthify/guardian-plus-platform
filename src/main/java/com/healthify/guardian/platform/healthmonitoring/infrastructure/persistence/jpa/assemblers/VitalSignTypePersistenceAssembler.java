package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.VitalSignTypePersistenceEntity;

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
        return vitalSignType;
    }

    public static VitalSignTypePersistenceEntity toPersistenceFromDomain(VitalSignType vitalSignType) {
        if (vitalSignType == null) return null;
        var entity = new VitalSignTypePersistenceEntity();
        entity.setId(vitalSignType.getId().value());
        entity.setCode(vitalSignType.getCode().value());
        entity.setName(vitalSignType.getName());
        entity.setUnit(vitalSignType.getUnit());
        return entity;
    }
}
