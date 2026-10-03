package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertChannelSettingId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertChannelSettingPersistenceEntity;

/**
 * Static assembler between the {@link AlertChannelSetting} domain aggregate and its persistence entity.
 */
public final class AlertChannelSettingPersistenceAssembler {

    private AlertChannelSettingPersistenceAssembler() {
    }

    public static AlertChannelSetting toDomainFromPersistence(AlertChannelSettingPersistenceEntity entity) {
        if (entity == null) return null;
        var setting = new AlertChannelSetting();
        setting.setId(new AlertChannelSettingId(entity.getId()));
        setting.setUserId(entity.getUserId());
        setting.setChannel(entity.getChannel());
        setting.setEnabled(entity.isEnabled());
        setting.setDeviceToken(entity.getDeviceToken());
        return setting;
    }

    public static AlertChannelSettingPersistenceEntity toPersistenceFromDomain(AlertChannelSetting setting) {
        if (setting == null) return null;
        var entity = new AlertChannelSettingPersistenceEntity();
        entity.setId(setting.getId().value());
        entity.setUserId(setting.getUserId());
        entity.setChannel(setting.getChannel());
        entity.setEnabled(setting.isEnabled());
        entity.setDeviceToken(setting.getDeviceToken());
        return entity;
    }
}
