package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.SleepCycleRecordRepository;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.SleepCycleRecordPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.SleepCycleRecordPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * JPA adapter for the {@link SleepCycleRecordRepository} domain port.
 */
@Repository
public class SleepCycleRecordRepositoryImpl implements SleepCycleRecordRepository {

    /** Bounds used for an open-ended period, so the derived query never receives a null parameter. */
    private static final Instant UNBOUNDED_FROM = Instant.EPOCH;
    private static final Instant UNBOUNDED_TO = Instant.parse("9999-12-31T00:00:00Z");

    private final SleepCycleRecordPersistenceRepository sleepCycleRecordPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SleepCycleRecordRepositoryImpl(
            SleepCycleRecordPersistenceRepository sleepCycleRecordPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.sleepCycleRecordPersistenceRepository = sleepCycleRecordPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<SleepCycleRecord> findByPersonUnderCareId(PersonUnderCareId personUnderCareId, Instant from, Instant to) {
        return sleepCycleRecordPersistenceRepository
                .findByPersonUnderCareIdAndEndTimeGreaterThanEqualAndEndTimeLessThanOrderByStartTimeDesc(
                        personUnderCareId, from != null ? from : UNBOUNDED_FROM, to != null ? to : UNBOUNDED_TO)
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
