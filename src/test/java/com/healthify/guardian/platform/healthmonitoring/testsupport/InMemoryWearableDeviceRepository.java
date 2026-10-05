package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.SerialNumber;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

public class InMemoryWearableDeviceRepository implements WearableDeviceRepository {

    private final InMemoryAggregateStore<WearableDeviceId, WearableDevice> store;

    public InMemoryWearableDeviceRepository(ApplicationEventPublisher eventPublisher) {
        this.store = new InMemoryAggregateStore<>(WearableDevice::getId, eventPublisher);
    }

    @Override
    public WearableDevice save(WearableDevice device) {
        return store.save(device);
    }

    @Override
    public Optional<WearableDevice> findById(WearableDeviceId id) {
        return store.findById(id);
    }

    @Override
    public List<WearableDevice> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        return store.findAll(device -> device.getCareRecipientProfileId().equals(careRecipientProfileId));
    }

    @Override
    public Optional<WearableDevice> findBySerialNumber(SerialNumber serialNumber) {
        return store.findAll(device -> device.getSerialNumber().equals(serialNumber)).stream().findFirst();
    }

    @Override
    public List<WearableDevice> findAll() {
        return store.findAll(device -> true);
    }
}
