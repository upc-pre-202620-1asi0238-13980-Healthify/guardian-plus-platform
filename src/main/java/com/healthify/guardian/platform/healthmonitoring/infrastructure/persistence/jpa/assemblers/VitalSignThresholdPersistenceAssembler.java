package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignThresholdId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.VitalSignThresholdPersistenceEntity;

import java.util.Date;

/**
 * Static assembler between the {@link VitalSignThreshold} aggregate and its persistence entity.
 */
public final class VitalSignThresholdPersistenceAssembler {

    private VitalSignThresholdPersistenceAssembler() {
    }

    public static VitalSignThreshold toDomainFromPersistence(VitalSignThresholdPersistenceEntity entity) {
        if (entity == null) return null;
        var threshold = new VitalSignThreshold();
        threshold.setId(new VitalSignThresholdId(entity.getId()));
        threshold.setCareRecipientProfileId(entity.getCareRecipientProfileId());
        threshold.setVitalSignTypeId(new VitalSignTypeId(entity.getVitalSignTypeId()));
        threshold.setMinimumValue(entity.getMinimumValue());
        threshold.setMaximumValue(entity.getMaximumValue());
        threshold.setRequiredConsecutiveHits(entity.getRequiredConsecutiveHits());
        threshold.setActive(entity.getActive());
        threshold.setCreatedAt(toInstant(entity.getCreatedAt()));
        threshold.setUpdatedAt(toInstant(entity.getUpdatedAt()));
        return threshold;
    }

    public static VitalSignThresholdPersistenceEntity toPersistenceFromDomain(VitalSignThreshold threshold) {
        if (threshold == null) return null;
        var entity = new VitalSignThresholdPersistenceEntity();
        entity.setId(threshold.getId().value());
        entity.setCareRecipientProfileId(threshold.getCareRecipientProfileId());
        entity.setVitalSignTypeId(threshold.getVitalSignTypeId().value());
        entity.setMinimumValue(threshold.getMinimumValue());
        entity.setMaximumValue(threshold.getMaximumValue());
        entity.setRequiredConsecutiveHits(threshold.getRequiredConsecutiveHits());
        entity.setActive(threshold.getActive());
        return entity;
    }

    private static java.time.Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
