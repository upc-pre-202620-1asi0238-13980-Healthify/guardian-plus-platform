package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.converters.CareRecipientProfileIdPersistenceConverter;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.converters.UserIdPersistenceConverter;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.embeddables.AlertSourceEmbeddable;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity for alerts, owning their deliveries and responses.
 *
 * <p>Both child collections are fetched eagerly with one sub-select each: an alert is always
 * rebuilt as a whole aggregate, including by schedulers that run outside any web request (and
 * therefore without open-session-in-view), and sub-select fetching avoids both lazy-loading
 * failures and the cartesian product of joining two bags. Both are read back in the order they were
 * added, from a position written once at insert time (never updated, so saving an alert does not
 * rewrite its children's rows).</p>
 */
@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "ix_alerts_care_recipient_profile_id", columnList = "care_recipient_profile_id"),
        @Index(name = "ix_alerts_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class AlertPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = CareRecipientProfileIdPersistenceConverter.class)
    @Column(name = "care_recipient_profile_id", nullable = false)
    private CareRecipientProfileId careRecipientProfileId;

    @Embedded
    private AlertSourceEmbeddable source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertStatus status;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "last_dispatched_at")
    private Instant lastDispatchedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Convert(converter = UserIdPersistenceConverter.class)
    @Column(name = "acknowledged_by_user_id")
    private UserId acknowledgedByUserId;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("dispatchOrder ASC")
    private List<AlertDeliveryPersistenceEntity> deliveries = new ArrayList<>();

    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("claimOrder ASC")
    private List<AlertResponsePersistenceEntity> responses = new ArrayList<>();
}
