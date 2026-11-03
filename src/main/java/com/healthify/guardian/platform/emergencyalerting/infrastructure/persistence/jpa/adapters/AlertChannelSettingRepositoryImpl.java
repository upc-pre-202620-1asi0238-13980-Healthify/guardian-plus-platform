package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertChannelSettingRepository;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers.AlertChannelSettingPersistenceAssembler;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories.AlertChannelSettingPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link AlertChannelSettingRepository} domain port.
 */
@Repository
public class AlertChannelSettingRepositoryImpl implements AlertChannelSettingRepository {

    private final AlertChannelSettingPersistenceRepository alertChannelSettingPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AlertChannelSettingRepositoryImpl(
            AlertChannelSettingPersistenceRepository alertChannelSettingPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.alertChannelSettingPersistenceRepository = alertChannelSettingPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AlertChannelSetting save(AlertChannelSetting setting) {
        var savedEntity = alertChannelSettingPersistenceRepository.save(
                AlertChannelSettingPersistenceAssembler.toPersistenceFromDomain(setting));
        var saved = AlertChannelSettingPersistenceAssembler.toDomainFromPersistence(savedEntity);
        var events = List.copyOf(setting.domainEvents());
        setting.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        return saved;
    }

    @Override
    public List<AlertChannelSetting> findByUserId(UserId userId) {
        return alertChannelSettingPersistenceRepository.findByUserId(userId).stream()
                .map(AlertChannelSettingPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<AlertChannelSetting> findByUserIdIn(Collection<UserId> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return alertChannelSettingPersistenceRepository.findByUserIdIn(userIds).stream()
                .map(AlertChannelSettingPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<AlertChannelSetting> findByUserIdAndChannel(UserId userId, NotificationChannel channel) {
        return alertChannelSettingPersistenceRepository.findByUserIdAndChannel(userId, channel)
                .map(AlertChannelSettingPersistenceAssembler::toDomainFromPersistence);
    }
}
