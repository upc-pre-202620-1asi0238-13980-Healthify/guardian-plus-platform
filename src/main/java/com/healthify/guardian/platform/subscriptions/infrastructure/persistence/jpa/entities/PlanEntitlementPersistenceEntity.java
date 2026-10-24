package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_entitlements")
public class PlanEntitlementPersistenceEntity {

    @EmbeddedId
    private PlanEntitlementId id;

    @MapsId("planId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanPersistenceEntity plan;

    @MapsId("entitlementCode")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entitlement_code", nullable = false)
    private EntitlementPersistenceEntity entitlement;

    protected PlanEntitlementPersistenceEntity() {
    }

    public PlanEntitlementPersistenceEntity(
            PlanPersistenceEntity plan,
            EntitlementPersistenceEntity entitlement
    ) {
        this.plan = plan;
        this.entitlement = entitlement;
        this.id = new PlanEntitlementId(
                plan.getId(),
                entitlement.getCode()
        );
    }

    public PlanEntitlementId getId() {
        return id;
    }

    public PlanPersistenceEntity getPlan() {
        return plan;
    }

    public EntitlementPersistenceEntity getEntitlement() {
        return entitlement;
    }
}