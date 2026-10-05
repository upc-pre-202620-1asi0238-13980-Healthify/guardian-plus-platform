package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for the vital sign type catalog.
 */
@Entity
@Table(name = "vital_sign_types")
@Getter
@Setter
@NoArgsConstructor
public class VitalSignTypePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;
}
