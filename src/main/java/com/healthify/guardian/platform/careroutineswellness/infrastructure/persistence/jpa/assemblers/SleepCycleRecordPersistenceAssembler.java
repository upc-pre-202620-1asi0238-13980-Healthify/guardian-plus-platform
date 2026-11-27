package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepCycleRecordId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.SleepCycleRecordPersistenceEntity;

/**
 * Static assembler between the {@link SleepCycleRecord} domain aggregate and its persistence entity.
 */
public final class SleepCycleRecordPersistenceAssembler {

    private SleepCycleRecordPersistenceAssembler() {
    }

    public static SleepCycleRecord toDomainFromPersistence(SleepCycleRecordPersistenceEntity entity) {
        if (entity == null) return null;
        var record = new SleepCycleRecord();
        record.setId(new SleepCycleRecordId(entity.getId()));
        record.setPersonUnderCareId(entity.getPersonUnderCareId());
        record.setStartTime(entity.getStartTime());
        record.setEndTime(entity.getEndTime());
        record.setInterruptionCount(entity.getInterruptionCount());
        record.setClassification(entity.getClassification());
        return record;
    }

    public static SleepCycleRecordPersistenceEntity toPersistenceFromDomain(SleepCycleRecord record) {
        if (record == null) return null;
        var entity = new SleepCycleRecordPersistenceEntity();
        entity.setId(record.getId().value());
        entity.setPersonUnderCareId(record.getPersonUnderCareId());
        entity.setStartTime(record.getStartTime());
        entity.setEndTime(record.getEndTime());
        entity.setInterruptionCount(record.getInterruptionCount());
        entity.setClassification(record.getClassification());
        return entity;
    }
}
