package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.converters.CareRecipientProfileIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for vital sign readings. One row per reading of one vital sign type.
 */
@Entity
@Table(name = "vital_sign_readings", indexes = {
        @Index(name = "idx_vital_sign_readings_recipient_type_measured",
                columnList = "care_recipient_profile_id, vital_sign_type, measured_at"),
        @Index(name = "idx_vital_sign_readings_device_type_measured",
                columnList = "wearable_device_id, vital_sign_type, measured_at")
})
@Getter
@Setter
@NoArgsConstructor
public class VitalSignPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "wearable_device_id", nullable = false)
    private UUID wearableDeviceId;

    @Convert(converter = CareRecipientProfileIdPersistenceConverter.class)
    @Column(name = "care_recipient_profile_id", nullable = false)
    private CareRecipientProfileId careRecipientProfileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vital_sign_type", nullable = false, length = 20)
    private VitalSignType vitalSignType;

    @Column(name = "value", nullable = false, precision = 12, scale = 3)
    private BigDecimal value;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "emitted_at")
    private Instant emittedAt;
}
