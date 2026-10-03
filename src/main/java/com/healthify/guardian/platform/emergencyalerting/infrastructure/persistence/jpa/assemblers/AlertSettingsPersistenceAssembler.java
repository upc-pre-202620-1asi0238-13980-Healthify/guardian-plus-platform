package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AckTimeout;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSettingsId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertSettingsPersistenceEntity;

/**
 * Static assembler between the {@link AlertSettings} domain aggregate and its persistence entity.
 */
public final class AlertSettingsPersistenceAssembler {

    private AlertSettingsPersistenceAssembler() {
    }

    public static AlertSettings toDomainFromPersistence(AlertSettingsPersistenceEntity entity) {
        if (entity == null) return null;
        var settings = new AlertSettings();
        settings.setId(new AlertSettingsId(entity.getId()));
        settings.setCareRecipientProfileId(entity.getCareRecipientProfileId());
        settings.setPrimaryAckTimeout(new AckTimeout(entity.getPrimaryAckTimeoutSec()));
        settings.setEscalationEnabled(entity.isEscalationEnabled());
        settings.setSilentModeEnabled(entity.isSilentModeEnabled());
        settings.setBroadcastCriticalImmediately(entity.isBroadcastCriticalImmediately());
        return settings;
    }

    public static AlertSettingsPersistenceEntity toPersistenceFromDomain(AlertSettings settings) {
        if (settings == null) return null;
        var entity = new AlertSettingsPersistenceEntity();
        entity.setId(settings.getId().value());
        entity.setCareRecipientProfileId(settings.getCareRecipientProfileId());
        entity.setPrimaryAckTimeoutSec(settings.getPrimaryAckTimeout().seconds());
        entity.setEscalationEnabled(settings.isEscalationEnabled());
        entity.setSilentModeEnabled(settings.isSilentModeEnabled());
        entity.setBroadcastCriticalImmediately(settings.isBroadcastCriticalImmediately());
        return entity;
    }
}
