package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.HealthReportRepository;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers.HealthReportPersistenceAssembler;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories.HealthReportPersistenceRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link HealthReportRepository} domain port.
 */
@Repository
public class HealthReportRepositoryImpl implements HealthReportRepository {

    private final HealthReportPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public HealthReportRepositoryImpl(HealthReportPersistenceRepository persistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public HealthReport save(HealthReport report) {
        var savedEntity = persistenceRepository.save(HealthReportPersistenceAssembler.toPersistenceFromDomain(report));
        report.domainEvents().forEach(eventPublisher::publishEvent);
        report.clearDomainEvents();
        return HealthReportPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public Optional<HealthReport> findById(HealthReportId id) {
        return persistenceRepository.findById(id.value()).map(HealthReportPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<HealthReport> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        return persistenceRepository.findByCareRecipientProfileIdOrderByGeneratedAtDesc(careRecipientProfileId)
                .stream().map(HealthReportPersistenceAssembler::toDomainFromPersistence).toList();
    }
}
