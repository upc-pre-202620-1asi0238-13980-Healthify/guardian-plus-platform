package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.SleepCycleRecordRepository;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.SleepCycleRecordPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.SleepCycleRecordPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA adapter for the {@link SleepCycleRecordRepository} domain port.
 */
@Repository
public class SleepCycleRecordRepositoryImpl implements SleepCycleRecordRepository {

    private final SleepCycleRecordPersistenceRepository sleepCycleRecordPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SleepCycleRecordRepositoryImpl(
            SleepCycleRecordPersistenceRepository sleepCycleRecordPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.sleepCycleRecordPersistenceRepository = sleepCycleRecordPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<SleepCycleRecord> findByPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        return sleepCycleRecordPersistenceRepository.findByPersonUnderCareIdOrderByStartTimeDesc(personUnderCareId)
                .stream()
                .map(SleepCycleRecordPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public SleepCycleRecord save(SleepCycleRecord record) {
        var entity = SleepCycleRecordPersistenceAssembler.toPersistenceFromDomain(record);
        var savedEntity = sleepCycleRecordPersistenceRepository.save(entity);
        record.domainEvents().forEach(eventPublisher::publishEvent);
        record.clearDomainEvents();
        return SleepCycleRecordPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }
}
