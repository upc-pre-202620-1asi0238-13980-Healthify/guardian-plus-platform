package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(
                        name = "ix_subscriptions_subscriber_id",
                        columnList = "subscriber_id"
                ),
                @Index(
                        name = "ix_subscriptions_status",
                        columnList = "status"
                )
        }
)
public class SubscriptionPersistenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "subscriber_id",
            nullable = false,
            updatable = false
    )
    private UUID subscriberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanPersistenceEntity plan;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private SubscriptionStatus status;

    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    @Column(name = "cancel_at_period_end", nullable = false)
    private boolean cancelAtPeriodEnd;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SubscriptionPersistenceEntity() {
    }

    public SubscriptionPersistenceEntity(
            UUID id,
            UUID subscriberId,
            PlanPersistenceEntity plan,
            SubscriptionStatus status,
            Instant currentPeriodStart,
            Instant currentPeriodEnd,
            boolean cancelAtPeriodEnd,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.subscriberId = subscriberId;
        this.plan = plan;
        this.status = status;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSubscriberId() {
        return subscriberId;
    }

    public PlanPersistenceEntity getPlan() {
        return plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public Instant getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public boolean isCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}