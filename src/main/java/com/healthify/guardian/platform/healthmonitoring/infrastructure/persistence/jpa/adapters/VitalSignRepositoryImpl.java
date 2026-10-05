package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers.VitalSignPersistenceAssembler;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories.VitalSignPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link VitalSignRepository} domain port.
 */
@Repository
public class VitalSignRepositoryImpl implements VitalSignRepository {

    private final VitalSignPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VitalSignRepositoryImpl(VitalSignPersistenceRepository persistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public VitalSign save(VitalSign vitalSign) {
        var savedEntity = persistenceRepository.save(VitalSignPersistenceAssembler.toPersistenceFromDomain(vitalSign));
        vitalSign.domainEvents().forEach(eventPublisher::publishEvent);
        vitalSign.clearDomainEvents();
        return VitalSignPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public List<VitalSign> saveAll(List<VitalSign> vitalSigns) {
        // Persist every reading before publishing any event, so handlers looking at recent
        // readings (tolerance rule) see the whole batch
        var savedEntities = persistenceRepository.saveAll(vitalSigns.stream().map(VitalSignPersistenceAssembler::toPersistenceFromDomain).toList());
        vitalSigns.forEach(vitalSign -> {
            vitalSign.domainEvents().forEach(eventPublisher::publishEvent);
            vitalSign.clearDomainEvents();
        });
        return savedEntities.stream().map(VitalSignPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public Optional<VitalSign> findById(VitalSignId id) {
        return persistenceRepository.findById(id.value()).map(VitalSignPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<VitalSign> findLatestByCareRecipientProfileIdAndVitalSignTypeId(
            CareRecipientProfileId careRecipientProfileId, VitalSignTypeId vitalSignTypeId) {
        return persistenceRepository
                .findFirstByCareRecipientProfileIdAndVitalSignTypeIdAndEmittedAtIsNotNullOrderByMeasuredAtDesc(
                        careRecipientProfileId, vitalSignTypeId.value())
                .map(VitalSignPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<VitalSign> findRecentByCareRecipientProfileIdAndVitalSignTypeId(
            CareRecipientProfileId careRecipientProfileId, VitalSignTypeId vitalSignTypeId, int count) {
        return persistenceRepository.findByCareRecipientProfileIdAndVitalSignTypeIdOrderByMeasuredAtDesc(
                        careRecipientProfileId, vitalSignTypeId.value(), PageRequest.of(0, count))
                .stream().map(VitalSignPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public List<VitalSign> findByCareRecipientProfileIdAndPeriod(CareRecipientProfileId careRecipientProfileId, DateRange period) {
        var from = period.startDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        var to = period.endDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return persistenceRepository
                .findByCareRecipientProfileIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanOrderByMeasuredAtAsc(
                        careRecipientProfileId, from, to)
                .stream().map(VitalSignPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public boolean existsByWearableDeviceIdAndVitalSignTypeIdAndMeasuredAt(
            WearableDeviceId wearableDeviceId, VitalSignTypeId vitalSignTypeId, Instant measuredAt) {
        return persistenceRepository.existsByWearableDeviceIdAndVitalSignTypeIdAndMeasuredAt(
                wearableDeviceId.value(), vitalSignTypeId.value(), measuredAt);
    }
}
