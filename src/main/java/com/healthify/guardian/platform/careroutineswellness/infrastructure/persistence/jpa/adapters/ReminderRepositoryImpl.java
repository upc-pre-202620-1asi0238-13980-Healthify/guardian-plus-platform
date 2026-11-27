package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.ReminderPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.ReminderPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JPA adapter for the {@link ReminderRepository} domain port.
 */
@Repository
public class ReminderRepositoryImpl implements ReminderRepository {

    /** Bounds used for an open-ended period, so the derived queries never receive a null parameter. */
    private static final Instant UNBOUNDED_FROM = Instant.EPOCH;
    private static final Instant UNBOUNDED_TO = Instant.parse("9999-12-31T00:00:00Z");
    private static final Set<ReminderStatus> ACTIVE_STATUSES =
            Set.of(ReminderStatus.SCHEDULED, ReminderStatus.ISSUED, ReminderStatus.REISSUED);

    private final ReminderPersistenceRepository reminderPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReminderRepositoryImpl(
            ReminderPersistenceRepository reminderPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.reminderPersistenceRepository = reminderPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Reminder> findById(ReminderId id) {
        return reminderPersistenceRepository.findById(id.value())
                .map(ReminderPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Reminder> findByPersonUnderCareId(
            PersonUnderCareId personUnderCareId, Instant from, Instant to, ReminderType type) {
        var lower = from != null ? from : UNBOUNDED_FROM;
        var upper = to != null ? to : UNBOUNDED_TO;
        var entities = type == null
                ? reminderPersistenceRepository
                        .findByPersonUnderCareIdAndScheduledTimeGreaterThanEqualAndScheduledTimeLessThanOrderByScheduledTime(
                                personUnderCareId, lower, upper)
                : reminderPersistenceRepository
                        .findByPersonUnderCareIdAndTypeAndScheduledTimeGreaterThanEqualAndScheduledTimeLessThanOrderByScheduledTime(
                                personUnderCareId, type, lower, upper);
        return entities.stream()
                .map(ReminderPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Reminder> findActiveByPersonUnderCareIdAndType(PersonUnderCareId personUnderCareId, ReminderType type) {
        return reminderPersistenceRepository
                .findByPersonUnderCareIdAndTypeAndStatusInOrderByScheduledTime(personUnderCareId, type, ACTIVE_STATUSES)
                .stream()
                .map(ReminderPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Reminder> findDueForIssuance(Instant currentTime) {
        return reminderPersistenceRepository.findDueForIssuance(ReminderStatus.SCHEDULED, currentTime).stream()
                .map(ReminderPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Reminder> findAwaitingConfirmation() {
        return reminderPersistenceRepository.findByStatus(ReminderStatus.ISSUED).stream()
                .map(ReminderPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Reminder save(Reminder reminder) {
        var entity = ReminderPersistenceAssembler.toPersistenceFromDomain(reminder);
        var savedEntity = reminderPersistenceRepository.save(entity);
        reminder.domainEvents().forEach(eventPublisher::publishEvent);
        reminder.clearDomainEvents();
        return ReminderPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }
}
