package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertSettingsRepository;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers.AlertSettingsPersistenceAssembler;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories.AlertSettingsPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link AlertSettingsRepository} domain port.
 */
@Repository
public class AlertSettingsRepositoryImpl implements AlertSettingsRepository {

    private final AlertSettingsPersistenceRepository alertSettingsPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AlertSettingsRepositoryImpl(
            AlertSettingsPersistenceRepository alertSettingsPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.alertSettingsPersistenceRepository = alertSettingsPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AlertSettings save(AlertSettings settings) {
        var savedEntity = alertSettingsPersistenceRepository.save(
                AlertSettingsPersistenceAssembler.toPersistenceFromDomain(settings));
        var saved = AlertSettingsPersistenceAssembler.toDomainFromPersistence(savedEntity);
        var events = List.copyOf(settings.domainEvents());
        settings.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        return saved;
    }

    @Override
    public Optional<AlertSettings> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        return alertSettingsPersistenceRepository.findByCareRecipientProfileId(careRecipientProfileId)
                .map(AlertSettingsPersistenceAssembler::toDomainFromPersistence);
    }
}
