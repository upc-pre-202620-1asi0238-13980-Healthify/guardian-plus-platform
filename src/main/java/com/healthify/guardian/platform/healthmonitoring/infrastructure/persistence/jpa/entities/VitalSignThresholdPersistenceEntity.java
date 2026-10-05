package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.converters.CareRecipientProfileIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA persistence entity for vital sign thresholds. One row per care recipient and vital sign type.
 */
@Entity
@Table(name = "vital_sign_thresholds", uniqueConstraints = @UniqueConstraint(
        name = "uk_vital_sign_thresholds_recipient_type",
        columnNames = {"care_recipient_profile_id", "vital_sign_type_id"}))
@Getter
@Setter
@NoArgsConstructor
public class VitalSignThresholdPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = CareRecipientProfileIdPersistenceConverter.class)
    @Column(name = "care_recipient_profile_id", nullable = false)
    private CareRecipientProfileId careRecipientProfileId;

    @Column(name = "vital_sign_type_id", nullable = false)
    private UUID vitalSignTypeId;

    @Column(name = "minimum_value", nullable = false, precision = 12, scale = 3)
    private BigDecimal minimumValue;

    @Column(name = "maximum_value", nullable = false, precision = 12, scale = 3)
    private BigDecimal maximumValue;

    @Column(name = "required_consecutive_hits", nullable = false)
    private Integer requiredConsecutiveHits;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
