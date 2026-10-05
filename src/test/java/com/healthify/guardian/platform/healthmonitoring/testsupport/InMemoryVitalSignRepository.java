package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InMemoryVitalSignRepository implements VitalSignRepository {

    private final InMemoryAggregateStore<VitalSignId, VitalSign> store;

    public InMemoryVitalSignRepository(ApplicationEventPublisher eventPublisher) {
        this.store = new InMemoryAggregateStore<>(VitalSign::getId, eventPublisher);
    }

    @Override
    public VitalSign save(VitalSign vitalSign) {
        return store.save(vitalSign);
    }

    @Override
    public List<VitalSign> saveAll(List<VitalSign> vitalSigns) {
        return store.saveAll(vitalSigns);
    }

    @Override
    public Optional<VitalSign> findById(VitalSignId id) {
        return store.findById(id);
    }

    @Override
    public Optional<VitalSign> findLatestByCareRecipientProfileIdAndVitalSignTypeId(
            CareRecipientProfileId careRecipientProfileId, VitalSignTypeId vitalSignTypeId) {
        return byRecipientAndType(careRecipientProfileId, vitalSignTypeId).stream()
                .filter(VitalSign::isEmitted)
                .findFirst();
    }

    @Override
    public List<VitalSign> findRecentByCareRecipientProfileIdAndVitalSignTypeId(
            CareRecipientProfileId careRecipientProfileId, VitalSignTypeId vitalSignTypeId, int count) {
        return byRecipientAndType(careRecipientProfileId, vitalSignTypeId).stream().limit(count).toList();
    }

    @Override
    public List<VitalSign> findByCareRecipientProfileIdAndPeriod(CareRecipientProfileId careRecipientProfileId, DateRange period) {
        var from = period.startDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        var to = period.endDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return store.findAll(vitalSign -> vitalSign.getCareRecipientProfileId().equals(careRecipientProfileId)
                        && !vitalSign.getMeasuredAt().isBefore(from) && vitalSign.getMeasuredAt().isBefore(to))
                .stream().sorted(Comparator.comparing(VitalSign::getMeasuredAt)).toList();
    }

    @Override
    public boolean existsByWearableDeviceIdAndVitalSignTypeIdAndMeasuredAt(
            WearableDeviceId wearableDeviceId, VitalSignTypeId vitalSignTypeId, Instant measuredAt) {
        return !store.findAll(vitalSign -> vitalSign.getWearableDeviceId().equals(wearableDeviceId)
                && vitalSign.getVitalSignTypeId().equals(vitalSignTypeId)
                && vitalSign.getMeasuredAt().equals(measuredAt)).isEmpty();
    }

    public List<VitalSign> findAll() {
        return store.findAll(vitalSign -> true);
    }

    private List<VitalSign> byRecipientAndType(CareRecipientProfileId careRecipientProfileId, VitalSignTypeId vitalSignTypeId) {
        return store.findAll(vitalSign -> vitalSign.getCareRecipientProfileId().equals(careRecipientProfileId)
                        && vitalSign.getVitalSignTypeId().equals(vitalSignTypeId))
                .stream().sorted(Comparator.comparing(VitalSign::getMeasuredAt).reversed()).toList();
    }
}
