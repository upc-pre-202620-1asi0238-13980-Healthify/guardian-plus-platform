package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.converters.CareRecipientProfileIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for alert settings. One row per Fragile Citizen at most.
 */
@Entity
@Table(name = "alert_settings")
@Getter
@Setter
@NoArgsConstructor
public class AlertSettingsPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = CareRecipientProfileIdPersistenceConverter.class)
    @Column(name = "care_recipient_profile_id", nullable = false, unique = true)
    private CareRecipientProfileId careRecipientProfileId;

    @Column(name = "primary_ack_timeout_sec", nullable = false)
    private Integer primaryAckTimeoutSec;

    @Column(name = "escalation_enabled", nullable = false)
    private boolean escalationEnabled;

    @Column(name = "silent_mode_enabled", nullable = false)
    private boolean silentModeEnabled;

    @Column(name = "broadcast_critical_immediately", nullable = false)
    private boolean broadcastCriticalImmediately;
}
