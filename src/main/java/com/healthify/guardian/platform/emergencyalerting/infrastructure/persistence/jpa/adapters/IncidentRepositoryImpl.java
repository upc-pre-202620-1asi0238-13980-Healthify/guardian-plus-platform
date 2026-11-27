package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.IncidentRepository;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers.IncidentPersistenceAssembler;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories.IncidentPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.IncidentPersistenceEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link IncidentRepository} domain port.
 */
@Repository
public class IncidentRepositoryImpl implements IncidentRepository {
    private static final Sort MOST_RECENT_FIRST =
        Sort.by(Sort.Direction.DESC, "markedInAttentionAt");
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
public Page<Incident> findAll(
        CareRecipientProfileId patientId,
        DateRange period,
        AlertSourceType eventType,
        Severity severity,
        Pageable pageable
) {
    Specification<IncidentPersistenceEntity> specification = (root, query, builder) -> {
        var alertSubquery = query.subquery(UUID.class);
        var alert = alertSubquery.from(AlertPersistenceEntity.class);
        var predicates = new ArrayList<Predicate>();

        predicates.add(
                builder.equal(
                        alert.get("id"),
                        root.get("alertId")
                )
        );

        if (patientId != null) {
            predicates.add(
                    builder.equal(
                            alert.get("careRecipientProfileId"),
                            patientId
                    )
            );
        }

        if (eventType != null) {
            predicates.add(
                    builder.equal(
                            alert.get("source").get("sourceType"),
                            eventType
                    )
            );
        }

        if (severity != null) {
            predicates.add(
                    builder.equal(
                            alert.get("severity"),
                            severity
                    )
            );
        }

        if (period != null && period.from() != null) {
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            alert.<Instant>get("triggeredAt"),
                            period.from()
                    )
            );
        }

        if (period != null && period.to() != null) {
            predicates.add(
                    builder.lessThanOrEqualTo(
                            alert.<Instant>get("triggeredAt"),
                            period.to()
                    )
            );
        }

        alertSubquery
                .select(alert.<UUID>get("id"))
                .where(predicates.toArray(Predicate[]::new));

        return builder.exists(alertSubquery);
    };

    var pageRequest = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            MOST_RECENT_FIRST
    );

    return incidentPersistenceRepository
            .findAll(specification, pageRequest)
            .map(IncidentPersistenceAssembler::toDomainFromPersistence);
}
    @Override
    public boolean existsByAlertId(AlertId alertId) {
        return incidentPersistenceRepository.existsByAlertId(alertId.value());
    }
}
