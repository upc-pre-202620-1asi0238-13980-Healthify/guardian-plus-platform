package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "subscription_entitlements")
public class SubscriptionEntitlementPersistenceEntity {

    @EmbeddedId
    private SubscriptionEntitlementId id;

    @MapsId("subscriptionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionPersistenceEntity subscription;

    @MapsId("entitlementCode")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entitlement_code", nullable = false)
    private EntitlementPersistenceEntity entitlement;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SubscriptionEntitlementPersistenceEntity() {
    }

    public SubscriptionEntitlementPersistenceEntity(
            SubscriptionPersistenceEntity subscription,
            EntitlementPersistenceEntity entitlement,
            Instant updatedAt
    ) {
        this.subscription = subscription;
        this.entitlement = entitlement;
        this.updatedAt = updatedAt;

        this.id = new SubscriptionEntitlementId(
                subscription.getId(),
                entitlement.getCode()
        );
    }

    public SubscriptionEntitlementId getId() {
        return id;
    }

    public SubscriptionPersistenceEntity getSubscription() {
        return subscription;
    }

    public EntitlementPersistenceEntity getEntitlement() {
        return entitlement;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}