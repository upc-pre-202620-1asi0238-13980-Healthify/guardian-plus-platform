package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

public class InMemoryVitalSignTypeRepository implements VitalSignTypeRepository {

    private final InMemoryAggregateStore<VitalSignTypeId, VitalSignType> store;

    public InMemoryVitalSignTypeRepository(ApplicationEventPublisher eventPublisher) {
        this.store = new InMemoryAggregateStore<>(VitalSignType::getId, eventPublisher);
    }

    @Override
    public VitalSignType save(VitalSignType vitalSignType) {
        return store.save(vitalSignType);
    }

    @Override
    public Optional<VitalSignType> findById(VitalSignTypeId id) {
        return store.findById(id);
    }

    @Override
    public Optional<VitalSignType> findByCode(VitalSignTypeCode code) {
        return store.findAll(type -> type.getCode().equals(code)).stream().findFirst();
    }

    @Override
    public List<VitalSignType> findAll() {
        return store.findAll(type -> true);
    }
}
