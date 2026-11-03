package com.healthify.guardian.platform.subscriptions.domain.model.entities;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.Money;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class Plan {

    private PlanId id;
    private String name;
    private Money price;
    private BillingCycle billingCycle;
    private boolean active;
    private Set<EntitlementCode> entitlementCodes =
            Collections.emptySet();

    public Plan() {
    }

    public Plan(
            PlanId id,
            String name,
            Money price,
            BillingCycle billingCycle,
            boolean active,
            Set<EntitlementCode> entitlementCodes) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "subscription.plan.id.invalid");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "subscription.plan.name.invalid");
        }

        if (price == null) {
            throw new IllegalArgumentException(
                    "subscription.plan.price.invalid");
        }

        if (billingCycle == null) {
            throw new IllegalArgumentException(
                    "subscription.plan.billing-cycle.invalid");
        }

        if (entitlementCodes == null) {
            throw new IllegalArgumentException(
                    "subscription.plan.entitlements.invalid");
        }

        this.id = id;
        this.name = name.strip();
        this.price = price;
        this.billingCycle = billingCycle;
        this.active = active;
        this.entitlementCodes =
                Collections.unmodifiableSet(
                        new LinkedHashSet<>(entitlementCodes));
    }

    public boolean isFree() {
        return price.isZero();
    }

    public boolean requiresPayment() {
        return !isFree();
    }

    public boolean includes(
            EntitlementCode entitlementCode) {

        return entitlementCode != null
                && entitlementCodes.contains(
                entitlementCode);
    }

    public void setId(PlanId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Money price) {
        this.price = price;
    }

    public void setBillingCycle(
            BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEntitlementCodes(
            Set<EntitlementCode> entitlementCodes) {

        this.entitlementCodes =
                entitlementCodes == null
                        ? Collections.emptySet()
                        : Collections.unmodifiableSet(
                        new LinkedHashSet<>(
                                entitlementCodes));
    }
}