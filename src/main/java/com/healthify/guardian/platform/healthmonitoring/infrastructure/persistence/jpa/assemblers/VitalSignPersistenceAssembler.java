package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignValue;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.VitalSignPersistenceEntity;

/**
 * Static assembler between the {@link VitalSign} aggregate and its persistence entity.
 */
public final class VitalSignPersistenceAssembler {

    private VitalSignPersistenceAssembler() {
    }

    public static VitalSign toDomainFromPersistence(VitalSignPersistenceEntity entity) {
        if (entity == null) return null;
        var vitalSign = new VitalSign();
        vitalSign.setId(new VitalSignId(entity.getId()));
        vitalSign.setWearableDeviceId(new WearableDeviceId(entity.getWearableDeviceId()));
        vitalSign.setCareRecipientProfileId(entity.getCareRecipientProfileId());
        vitalSign.setVitalSignTypeId(new VitalSignTypeId(entity.getVitalSignTypeId()));
        vitalSign.setValue(new VitalSignValue(entity.getValue()));
        vitalSign.setMeasuredAt(entity.getMeasuredAt());
        vitalSign.setReceivedAt(entity.getReceivedAt());
        vitalSign.setEmittedAt(entity.getEmittedAt());
        return vitalSign;
    }

    public static VitalSignPersistenceEntity toPersistenceFromDomain(VitalSign vitalSign) {
        if (vitalSign == null) return null;
        var entity = new VitalSignPersistenceEntity();
        entity.setId(vitalSign.getId().value());
        entity.setWearableDeviceId(vitalSign.getWearableDeviceId().value());
        entity.setCareRecipientProfileId(vitalSign.getCareRecipientProfileId());
        entity.setVitalSignTypeId(vitalSign.getVitalSignTypeId().value());
        entity.setValue(vitalSign.getValue().value());
        entity.setMeasuredAt(vitalSign.getMeasuredAt());
        entity.setReceivedAt(vitalSign.getReceivedAt());
        entity.setEmittedAt(vitalSign.getEmittedAt());
        return entity;
    }
}
