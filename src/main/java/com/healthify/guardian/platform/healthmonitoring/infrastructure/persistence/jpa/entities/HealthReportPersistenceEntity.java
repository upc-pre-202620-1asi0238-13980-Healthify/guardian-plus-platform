package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.converters.CareRecipientProfileIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA persistence entity for health reports. The {@code summary} column stores the serialized
 * per-type summaries and the recurrent anomalies count; there is no separate summaries table.
 */
@Entity
@Table(name = "health_reports")
@Getter
@Setter
@NoArgsConstructor
public class HealthReportPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = CareRecipientProfileIdPersistenceConverter.class)
    @Column(name = "care_recipient_profile_id", nullable = false)
    private CareRecipientProfileId careRecipientProfileId;

    @Column(name = "generated_by_user_id")
    private UUID generatedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private HealthReportType reportType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;
}
