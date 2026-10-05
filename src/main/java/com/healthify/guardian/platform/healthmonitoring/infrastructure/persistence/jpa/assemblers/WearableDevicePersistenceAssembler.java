package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.SerialNumber;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.WearableDevicePersistenceEntity;

import java.util.Date;

/**
 * Static assembler between the {@link WearableDevice} aggregate and its persistence entity.
 */
public final class WearableDevicePersistenceAssembler {

    private WearableDevicePersistenceAssembler() {
    }

    public static WearableDevice toDomainFromPersistence(WearableDevicePersistenceEntity entity) {
        if (entity == null) return null;
        var device = new WearableDevice();
        device.setId(new WearableDeviceId(entity.getId()));
        device.setCareRecipientProfileId(entity.getCareRecipientProfileId());
        device.setSerialNumber(new SerialNumber(entity.getSerialNumber()));
        device.setDeviceType(entity.getDeviceType());
        device.setStatus(entity.getStatus());
        device.setAssignedAt(entity.getAssignedAt());
        device.setCreatedAt(toInstant(entity.getCreatedAt()));
        device.setUpdatedAt(toInstant(entity.getUpdatedAt()));
        return device;
    }

    public static WearableDevicePersistenceEntity toPersistenceFromDomain(WearableDevice device) {
        if (device == null) return null;
        var entity = new WearableDevicePersistenceEntity();
        entity.setId(device.getId().value());
        entity.setCareRecipientProfileId(device.getCareRecipientProfileId());
        entity.setSerialNumber(device.getSerialNumber().value());
        entity.setDeviceType(device.getDeviceType());
        entity.setStatus(device.getStatus());
        entity.setAssignedAt(device.getAssignedAt());
        return entity;
    }

    private static java.time.Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
