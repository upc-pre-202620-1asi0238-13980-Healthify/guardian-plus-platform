package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.HydrationPlanRepository;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.HydrationPlanPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.HydrationPlanPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA adapter for the {@link HydrationPlanRepository} domain port.
 */
@Repository
public class HydrationPlanRepositoryImpl implements HydrationPlanRepository {

    private final HydrationPlanPersistenceRepository hydrationPlanPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public HydrationPlanRepositoryImpl(
            HydrationPlanPersistenceRepository hydrationPlanPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.hydrationPlanPersistenceRepository = hydrationPlanPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<HydrationPlan> findByPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        return hydrationPlanPersistenceRepository.findByPersonUnderCareId(personUnderCareId)
                .map(HydrationPlanPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public HydrationPlan save(HydrationPlan plan) {
        var entity = HydrationPlanPersistenceAssembler.toPersistenceFromDomain(plan);
        var savedEntity = hydrationPlanPersistenceRepository.save(entity);
        plan.domainEvents().forEach(eventPublisher::publishEvent);
        plan.clearDomainEvents();
        return HydrationPlanPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }
}
