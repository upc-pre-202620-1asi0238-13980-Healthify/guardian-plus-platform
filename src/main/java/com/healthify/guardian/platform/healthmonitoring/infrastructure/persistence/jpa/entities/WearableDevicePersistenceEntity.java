package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.converters.CareRecipientProfileIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DeviceType;

import java.time.Instant;

/**
 * JPA persistence entity for wearable devices.
 */
@Entity
@Table(name = "wearable_devices")
@Getter
@Setter
@NoArgsConstructor
public class WearableDevicePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = CareRecipientProfileIdPersistenceConverter.class)
    @Column(name = "care_recipient_profile_id", nullable = false)
    private CareRecipientProfileId careRecipientProfileId;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 30)
    private DeviceType deviceType;


    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;
}
