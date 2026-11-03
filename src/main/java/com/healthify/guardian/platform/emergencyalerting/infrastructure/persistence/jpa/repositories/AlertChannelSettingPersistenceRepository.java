package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertChannelSettingPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for alert channel setting persistence entities.
 */
@Repository
public interface AlertChannelSettingPersistenceRepository
        extends JpaRepository<AlertChannelSettingPersistenceEntity, UUID> {

    List<AlertChannelSettingPersistenceEntity> findByUserId(UserId userId);

    List<AlertChannelSettingPersistenceEntity> findByUserIdIn(Collection<UserId> userIds);

    Optional<AlertChannelSettingPersistenceEntity> findByUserIdAndChannel(UserId userId, NotificationChannel channel);
}
