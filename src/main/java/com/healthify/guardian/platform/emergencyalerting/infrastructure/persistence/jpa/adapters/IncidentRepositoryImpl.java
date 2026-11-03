package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.IncidentRepository;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers.IncidentPersistenceAssembler;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories.IncidentPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link IncidentRepository} domain port.
 */
@Repository
public class IncidentRepositoryImpl implements IncidentRepository {

    private final IncidentPersistenceRepository incidentPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public IncidentRepositoryImpl(
            IncidentPersistenceRepository incidentPersistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.incidentPersistenceRepository = incidentPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Incident save(Incident incident) {
        var savedEntity = incidentPersistenceRepository.save(IncidentPersistenceAssembler.toPersistenceFromDomain(incident));
        var saved = IncidentPersistenceAssembler.toDomainFromPersistence(savedEntity);
        var events = List.copyOf(incident.domainEvents());
        incident.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        return saved;
    }

    @Override
    public Optional<Incident> findById(IncidentId id) {
        return incidentPersistenceRepository.findById(id.value())
                .map(IncidentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Incident> findByAlertId(AlertId alertId) {
        return incidentPersistenceRepository.findByAlertId(alertId.value())
                .map(IncidentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByAlertId(AlertId alertId) {
        return incidentPersistenceRepository.existsByAlertId(alertId.value());
    }
}
