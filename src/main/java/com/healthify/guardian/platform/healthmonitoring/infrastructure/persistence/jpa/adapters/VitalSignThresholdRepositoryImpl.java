package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignThresholdId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignThresholdRepository;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers.VitalSignThresholdPersistenceAssembler;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories.VitalSignThresholdPersistenceRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link VitalSignThresholdRepository} domain port.
 */
@Repository
public class VitalSignThresholdRepositoryImpl implements VitalSignThresholdRepository {

    private final VitalSignThresholdPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VitalSignThresholdRepositoryImpl(VitalSignThresholdPersistenceRepository persistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public VitalSignThreshold save(VitalSignThreshold threshold) {
        var savedEntity = persistenceRepository.save(VitalSignThresholdPersistenceAssembler.toPersistenceFromDomain(threshold));
        threshold.domainEvents().forEach(eventPublisher::publishEvent);
        threshold.clearDomainEvents();
        return VitalSignThresholdPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public Optional<VitalSignThreshold> findById(VitalSignThresholdId id) {
        return persistenceRepository.findById(id.value()).map(VitalSignThresholdPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<VitalSignThreshold> findByCareRecipientProfileIdAndVitalSignTypeId(
            CareRecipientProfileId careRecipientProfileId, VitalSignTypeId vitalSignTypeId) {
        return persistenceRepository.findByCareRecipientProfileIdAndVitalSignTypeId(careRecipientProfileId, vitalSignTypeId.value())
                .map(VitalSignThresholdPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<VitalSignThreshold> findAllActiveByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        return persistenceRepository.findByCareRecipientProfileIdAndActiveTrue(careRecipientProfileId)
                .stream().map(VitalSignThresholdPersistenceAssembler::toDomainFromPersistence).toList();
    }
}
