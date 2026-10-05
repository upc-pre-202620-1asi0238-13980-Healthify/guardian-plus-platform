package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.SerialNumber;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers.WearableDevicePersistenceAssembler;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories.WearableDevicePersistenceRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link WearableDeviceRepository} domain port.
 */
@Repository
public class WearableDeviceRepositoryImpl implements WearableDeviceRepository {

    private final WearableDevicePersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public WearableDeviceRepositoryImpl(WearableDevicePersistenceRepository persistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public WearableDevice save(WearableDevice device) {
        var savedEntity = persistenceRepository.save(WearableDevicePersistenceAssembler.toPersistenceFromDomain(device));
        device.domainEvents().forEach(eventPublisher::publishEvent);
        device.clearDomainEvents();
        return WearableDevicePersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public Optional<WearableDevice> findById(WearableDeviceId id) {
        return persistenceRepository.findById(id.value()).map(WearableDevicePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<WearableDevice> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        return persistenceRepository.findByCareRecipientProfileId(careRecipientProfileId)
                .stream().map(WearableDevicePersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public Optional<WearableDevice> findBySerialNumber(SerialNumber serialNumber) {
        return persistenceRepository.findBySerialNumber(serialNumber.value()).map(WearableDevicePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<WearableDevice> findAll() {
        return persistenceRepository.findAll()
                .stream().map(WearableDevicePersistenceAssembler::toDomainFromPersistence).toList();
    }
}
