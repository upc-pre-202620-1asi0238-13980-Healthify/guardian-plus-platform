package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignThresholdId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignThresholdRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

public class InMemoryVitalSignThresholdRepository implements VitalSignThresholdRepository {

    private final InMemoryAggregateStore<VitalSignThresholdId, VitalSignThreshold> store;

    public InMemoryVitalSignThresholdRepository(ApplicationEventPublisher eventPublisher) {
        this.store = new InMemoryAggregateStore<>(VitalSignThreshold::getId, eventPublisher);
    }

    @Override
    public VitalSignThreshold save(VitalSignThreshold threshold) {
        return store.save(threshold);
    }

    @Override
    public Optional<VitalSignThreshold> findById(VitalSignThresholdId id) {
        return store.findById(id);
    }

    @Override
    public Optional<VitalSignThreshold> findByCareRecipientProfileIdAndVitalSignTypeId(
            CareRecipientProfileId careRecipientProfileId, VitalSignTypeId vitalSignTypeId) {
        return store.findAll(threshold -> threshold.getCareRecipientProfileId().equals(careRecipientProfileId)
                && threshold.getVitalSignTypeId().equals(vitalSignTypeId)).stream().findFirst();
    }

    @Override
    public List<VitalSignThreshold> findAllActiveByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        return store.findAll(threshold -> threshold.getCareRecipientProfileId().equals(careRecipientProfileId)
                && threshold.isActive());
    }
}
