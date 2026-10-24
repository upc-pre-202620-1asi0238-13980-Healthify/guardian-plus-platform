package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityLogEntryRepository;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.ActivityLogEntryPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.ActivityLogEntryPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA adapter for the {@link ActivityLogEntryRepository} domain port.
 */
@Repository
public class ActivityLogEntryRepositoryImpl implements ActivityLogEntryRepository {

    private final ActivityLogEntryPersistenceRepository activityLogEntryPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ActivityLogEntryRepositoryImpl(
            ActivityLogEntryPersistenceRepository activityLogEntryPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.activityLogEntryPersistenceRepository = activityLogEntryPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<ActivityLogEntry> findRecentByPersonUnderCareId(PersonUnderCareId personUnderCareId, int limit) {
        return activityLogEntryPersistenceRepository
                .findByPersonUnderCareIdOrderByOccurredAtDesc(personUnderCareId, PageRequest.of(0, limit))
                .stream()
                .map(ActivityLogEntryPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public ActivityLogEntry save(ActivityLogEntry entry) {
        var entity = ActivityLogEntryPersistenceAssembler.toPersistenceFromDomain(entry);
        var savedEntity = activityLogEntryPersistenceRepository.save(entity);
        entry.domainEvents().forEach(eventPublisher::publishEvent);
        entry.clearDomainEvents();
        return ActivityLogEntryPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }
}
