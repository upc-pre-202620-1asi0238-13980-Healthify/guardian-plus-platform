package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlanEntitlementId implements Serializable {

    @Column(name = "plan_id")
    private UUID planId;

    @Column(name = "entitlement_code", length = 100)
    private String entitlementCode;

    protected PlanEntitlementId() {
    }

    public PlanEntitlementId(
            UUID planId,
            String entitlementCode
    ) {
        this.planId = planId;
        this.entitlementCode = entitlementCode;
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getEntitlementCode() {
        return entitlementCode;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PlanEntitlementId that)) {
            return false;
        }

        return Objects.equals(planId, that.planId)
                && Objects.equals(entitlementCode, that.entitlementCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, entitlementCode);
    }
}