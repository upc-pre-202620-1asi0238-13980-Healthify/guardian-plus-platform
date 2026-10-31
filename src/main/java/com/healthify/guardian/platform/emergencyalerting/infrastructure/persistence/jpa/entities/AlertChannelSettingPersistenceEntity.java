package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.converters.UserIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for alert channel settings. One row per user and channel.
 */
@Entity
@Table(name = "alert_channel_settings", uniqueConstraints = @UniqueConstraint(
        name = "uk_alert_channel_settings_user_channel",
        columnNames = {"user_id", "channel"}))
@Getter
@Setter
@NoArgsConstructor
public class AlertChannelSettingPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = UserIdPersistenceConverter.class)
    @Column(name = "user_id", nullable = false)
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "device_token", length = 512)
    private String deviceToken;
}
