package com.healthify.guardian.platform.subscriptions.domain.model.aggregates;

import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class EntitlementSet
        extends AbstractDomainAggregateRoot<EntitlementSet> {

    private SubscriptionId subscriptionId;
    private Set<Entitlement> entitlements =
            Collections.emptySet();
    private Instant updatedAt;

    public EntitlementSet() {
    }

    public EntitlementSet(
            SubscriptionId subscriptionId,
            Set<Entitlement> entitlements,
            Instant updatedAt) {

        if (subscriptionId == null) {
            throw new IllegalArgumentException(
                    "subscription.id.invalid");
        }

        validateTimestamp(updatedAt);

        this.subscriptionId = subscriptionId;
        this.entitlements =
                immutableCopy(entitlements);
        this.updatedAt = updatedAt;
    }

    public void replaceWith(
            Set<Entitlement> entitlements,
            Instant updatedAt) {

        validateTimestamp(updatedAt);

        this.entitlements =
                immutableCopy(entitlements);
        this.updatedAt = updatedAt;
    }

    public void grant(
            Entitlement entitlement,
            Instant updatedAt) {

        if (entitlement == null) {
            throw new IllegalArgumentException(
                    "subscription.entitlement.invalid");
        }

        validateTimestamp(updatedAt);

        var updated =
                new LinkedHashSet<>(
                        this.entitlements);

        updated.removeIf(existing ->
                existing.matches(
                        entitlement.getCode()));

        updated.add(entitlement);

        this.entitlements =
                Collections.unmodifiableSet(updated);

        this.updatedAt = updatedAt;
    }

    public void revoke(
            EntitlementCode code,
            Instant updatedAt) {

        if (code == null) {
            throw new IllegalArgumentException(
                    "subscription.entitlement-code.invalid");
        }

        validateTimestamp(updatedAt);

        var updated =
                new LinkedHashSet<>(
                        this.entitlements);

        updated.removeIf(
                entitlement ->
                        entitlement.matches(code));

        this.entitlements =
                Collections.unmodifiableSet(updated);

        this.updatedAt = updatedAt;
    }

    public boolean contains(
            EntitlementCode code) {

        if (code == null) {
            return false;
        }

        return entitlements.stream()
                .anyMatch(
                        entitlement ->
                                entitlement.matches(code));
    }

    public void revokeAll(
            Instant updatedAt) {

        validateTimestamp(updatedAt);

        this.entitlements =
                Collections.emptySet();

        this.updatedAt = updatedAt;
    }

    private static Set<Entitlement> immutableCopy(
            Set<Entitlement> entitlements) {

        if (entitlements == null) {
            throw new IllegalArgumentException(
                    "subscription.entitlements.invalid");
        }

        return Collections.unmodifiableSet(
                new LinkedHashSet<>(
                        entitlements));
    }

    private static void validateTimestamp(
            Instant timestamp) {

        if (timestamp == null) {
            throw new IllegalArgumentException(
                    "subscription.timestamp.invalid");
        }
    }

    public void setSubscriptionId(
            SubscriptionId subscriptionId) {
        this.subscriptionId =
                subscriptionId;
    }

    public void setEntitlements(
            Set<Entitlement> entitlements) {
        this.entitlements =
                immutableCopy(entitlements);
    }

    public void setUpdatedAt(
            Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}