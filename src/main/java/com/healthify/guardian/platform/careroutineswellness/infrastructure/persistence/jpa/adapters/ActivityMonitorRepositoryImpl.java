package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityMonitorRepository;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.ActivityMonitorPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.ActivityMonitorPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA adapter for the {@link ActivityMonitorRepository} domain port.
 */
@Repository
public class ActivityMonitorRepositoryImpl implements ActivityMonitorRepository {

    private final ActivityMonitorPersistenceRepository activityMonitorPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ActivityMonitorRepositoryImpl(
            ActivityMonitorPersistenceRepository activityMonitorPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.activityMonitorPersistenceRepository = activityMonitorPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<ActivityMonitor> findByPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        return activityMonitorPersistenceRepository.findByPersonUnderCareId(personUnderCareId)
                .map(ActivityMonitorPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public ActivityMonitor save(ActivityMonitor monitor) {
        var entity = ActivityMonitorPersistenceAssembler.toPersistenceFromDomain(monitor);
        var savedEntity = activityMonitorPersistenceRepository.save(entity);
        monitor.domainEvents().forEach(eventPublisher::publishEvent);
        monitor.clearDomainEvents();
        return ActivityMonitorPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }
}
