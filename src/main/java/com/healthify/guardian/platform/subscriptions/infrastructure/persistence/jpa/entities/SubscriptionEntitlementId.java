package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SubscriptionEntitlementId implements Serializable {

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "entitlement_code", length = 100)
    private String entitlementCode;

    protected SubscriptionEntitlementId() {
    }

    public SubscriptionEntitlementId(
            UUID subscriptionId,
            String entitlementCode
    ) {
        this.subscriptionId = subscriptionId;
        this.entitlementCode = entitlementCode;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public String getEntitlementCode() {
        return entitlementCode;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof SubscriptionEntitlementId that)) {
            return false;
        }

        return Objects.equals(subscriptionId, that.subscriptionId)
                && Objects.equals(entitlementCode, that.entitlementCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, entitlementCode);
    }
}