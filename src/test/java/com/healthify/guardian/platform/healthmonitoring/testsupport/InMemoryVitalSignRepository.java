package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
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
    public Optional<VitalSign> findLatestByCareRecipientProfileIdAndVitalSignType(
            CareRecipientProfileId careRecipientProfileId, VitalSignType vitalSignType) {
        return byRecipientAndType(careRecipientProfileId, vitalSignType).stream()
                .filter(VitalSign::isEmitted)
                .findFirst();
    }

    @Override
    public List<VitalSign> findRecentByCareRecipientProfileIdAndVitalSignType(
            CareRecipientProfileId careRecipientProfileId, VitalSignType vitalSignType, int count) {
        return byRecipientAndType(careRecipientProfileId, vitalSignType).stream().limit(count).toList();
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
    public boolean existsByWearableDeviceIdAndVitalSignTypeAndMeasuredAt(
            WearableDeviceId wearableDeviceId, VitalSignType vitalSignType, Instant measuredAt) {
        return !store.findAll(vitalSign -> vitalSign.getWearableDeviceId().equals(wearableDeviceId)
                && vitalSign.getVitalSignType() == vitalSignType
                && vitalSign.getMeasuredAt().equals(measuredAt)).isEmpty();
    }

    public List<VitalSign> findAll() {
        return store.findAll(vitalSign -> true);
    }

    private List<VitalSign> byRecipientAndType(CareRecipientProfileId careRecipientProfileId, VitalSignType vitalSignType) {
        return store.findAll(vitalSign -> vitalSign.getCareRecipientProfileId().equals(careRecipientProfileId)
                        && vitalSign.getVitalSignType() == vitalSignType)
                .stream().sorted(Comparator.comparing(VitalSign::getMeasuredAt).reversed()).toList();
    }
}
