package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers.VitalSignTypePersistenceAssembler;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories.VitalSignTypePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link VitalSignTypeRepository} domain port.
 */
@Repository
public class VitalSignTypeRepositoryImpl implements VitalSignTypeRepository {

    private final VitalSignTypePersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VitalSignTypeRepositoryImpl(VitalSignTypePersistenceRepository persistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public VitalSignType save(VitalSignType vitalSignType) {
        var savedEntity = persistenceRepository.save(VitalSignTypePersistenceAssembler.toPersistenceFromDomain(vitalSignType));
        vitalSignType.domainEvents().forEach(eventPublisher::publishEvent);
        vitalSignType.clearDomainEvents();
        return VitalSignTypePersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public Optional<VitalSignType> findById(VitalSignTypeId id) {
        return persistenceRepository.findById(id.value()).map(VitalSignTypePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<VitalSignType> findByCode(VitalSignTypeCode code) {
        return persistenceRepository.findByCode(code.value()).map(VitalSignTypePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<VitalSignType> findAll() {
        return persistenceRepository.findAll(Sort.by("code")).stream().map(VitalSignTypePersistenceAssembler::toDomainFromPersistence).toList();
    }
}
