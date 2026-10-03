package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.services.ReminderReissuePolicy;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.ReminderPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.ReminderPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link ReminderRepository} domain port.
 */
@Repository
public class ReminderRepositoryImpl implements ReminderRepository {

    private final ReminderPersistenceRepository reminderPersistenceRepository;
    private final ReminderReissuePolicy reminderReissuePolicy;
    private final ApplicationEventPublisher eventPublisher;

    public ReminderRepositoryImpl(
            ReminderPersistenceRepository reminderPersistenceRepository,
            ReminderReissuePolicy reminderReissuePolicy,
            ApplicationEventPublisher eventPublisher) {
        this.reminderPersistenceRepository = reminderPersistenceRepository;
        this.reminderReissuePolicy = reminderReissuePolicy;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Reminder> findById(ReminderId id) {
        return reminderPersistenceRepository.findById(id.value())
                .map(ReminderPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Reminder> findByPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        return reminderPersistenceRepository.findByPersonUnderCareId(personUnderCareId).stream()
                .map(ReminderPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Reminder> findDueForIssuance(Instant currentTime) {
        return reminderPersistenceRepository
                .findByStatusAndScheduledTimeLessThanEqual(ReminderStatus.SCHEDULED, currentTime).stream()
                .map(ReminderPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Reminder> findOverdueForReissue(Instant currentTime) {
        return reminderPersistenceRepository.findByStatus(ReminderStatus.ISSUED).stream()
                .map(ReminderPersistenceAssembler::toDomainFromPersistence)
                .filter(reminder -> reminderReissuePolicy.requiresReissue(reminder, currentTime))
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
