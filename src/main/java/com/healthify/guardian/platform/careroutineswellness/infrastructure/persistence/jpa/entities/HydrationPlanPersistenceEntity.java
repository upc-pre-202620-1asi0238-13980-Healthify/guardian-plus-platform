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

/**
 * JPA persistence entity for hydration plans. One row per person under care.
 */
@Entity
@Table(name = "hydration_plans")
@Getter
@Setter
@NoArgsConstructor
public class HydrationPlanPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = PersonUnderCareIdPersistenceConverter.class)
    @Column(name = "person_under_care_id", nullable = false, unique = true)
    private PersonUnderCareId personUnderCareId;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "daily_goal_glasses", nullable = false)
    private Integer dailyGoalGlasses;

    @Column(name = "interval_hours", nullable = false)
    private Integer intervalHours;

    @Column(name = "respect_sleep_window", nullable = false)
    private Boolean respectSleepWindow;
}
