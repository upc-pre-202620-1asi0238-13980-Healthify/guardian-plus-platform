package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.converters.PersonUnderCareIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA persistence entity for medication stocks. One row per person under care.
 */
@Entity
@Table(name = "medication_stocks")
@Getter
@Setter
@NoArgsConstructor
public class MedicationStockPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = PersonUnderCareIdPersistenceConverter.class)
    @Column(name = "person_under_care_id", nullable = false, unique = true)
    private PersonUnderCareId personUnderCareId;

    @Column(name = "remaining_doses", nullable = false)
    private Integer remainingDoses;

    @Column(name = "daily_consumption", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyConsumption;

    @Column(name = "last_acquisition_date")
    private Instant lastAcquisitionDate;
}
