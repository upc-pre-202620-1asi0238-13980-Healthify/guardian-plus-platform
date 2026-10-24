package com.healthify.guardian.platform.subscriptions.domain.model.aggregates;

import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Subscription
        extends AbstractDomainAggregateRoot<Subscription> {

    private SubscriptionId id;
    private SubscriberId subscriberId;
    private PlanId planId;
    private SubscriptionStatus status;
    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;
    private boolean cancelAtPeriodEnd;
    private Instant createdAt;
    private Instant updatedAt;

    public Subscription() {
    }

    /**
     * Creates a requested subscription.
     * It remains pending until activation succeeds.
     */
    public Subscription(
            SubscriberId subscriberId,
            PlanId planId,
            Instant createdAt) {

        if (subscriberId == null) {
            throw new IllegalArgumentException(
                    "subscription.subscriber-id.invalid");
        }

        if (planId == null) {
            throw new IllegalArgumentException(
                    "subscription.plan-id.invalid");
        }

        validateTimestamp(createdAt);

        this.id = SubscriptionId.generate();
        this.subscriberId = subscriberId;
        this.planId = planId;
        this.status = SubscriptionStatus.PENDING;
        this.cancelAtPeriodEnd = false;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void activate(
            PlanId planId,
            Instant periodStart,
            Instant periodEnd,
            Instant updatedAt) {

        if (status != SubscriptionStatus.PENDING) {
            throw new IllegalStateException(
                    "subscription.activation.invalid-state");
        }

        if (planId == null) {
            throw new IllegalArgumentException(
                    "subscription.plan-id.invalid");
        }

        validatePeriod(
                periodStart,
                periodEnd);

        validateTimestamp(updatedAt);

        this.planId = planId;
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = periodStart;
        this.currentPeriodEnd = periodEnd;
        this.cancelAtPeriodEnd = false;
        this.updatedAt = updatedAt;
    }

    public void changePlan(
            PlanId newPlanId,
            Instant updatedAt) {

        if (!isActive()) {
            throw new IllegalStateException(
                    "subscription.plan-change.invalid-state");
        }

        if (newPlanId == null) {
            throw new IllegalArgumentException(
                    "subscription.plan-id.invalid");
        }

        validateTimestamp(updatedAt);

        this.planId = newPlanId;
        this.updatedAt = updatedAt;
    }

    public void renew(
            Instant newPeriodEnd,
            Instant updatedAt) {

        if (status != SubscriptionStatus.ACTIVE
                || cancelAtPeriodEnd) {

            throw new IllegalStateException(
                    "subscription.renewal.invalid-state");
        }

        if (currentPeriodEnd == null
                || newPeriodEnd == null
                || !newPeriodEnd.isAfter(
                currentPeriodEnd)) {

            throw new IllegalArgumentException(
                    "subscription.period.invalid");
        }

        validateTimestamp(updatedAt);

        this.currentPeriodStart =
                this.currentPeriodEnd;

        this.currentPeriodEnd =
                newPeriodEnd;

        this.updatedAt =
                updatedAt;
    }

    public void requestCancellation(
            Instant updatedAt) {

        scheduleCancellation(updatedAt);
    }

    public void scheduleCancellation(
            Instant updatedAt) {

        if (status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "subscription.cancellation.invalid-state");
        }

        validateTimestamp(updatedAt);

        this.status =
                SubscriptionStatus.CANCELLATION_SCHEDULED;

        this.cancelAtPeriodEnd = true;
        this.updatedAt = updatedAt;
    }

    public void cancel(
            Instant updatedAt) {

        if (status == SubscriptionStatus.CANCELLED
                || status == SubscriptionStatus.EXPIRED) {

            throw new IllegalStateException(
                    "subscription.cancellation.invalid-state");
        }

        validateTimestamp(updatedAt);

        this.status =
                SubscriptionStatus.CANCELLED;

        this.cancelAtPeriodEnd = false;
        this.updatedAt = updatedAt;
    }

    public void expire(
            Instant currentDate) {

        validateTimestamp(currentDate);

        if (status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "subscription.expiration.invalid-state");
        }

        if (currentPeriodEnd == null
                || currentDate.isBefore(
                currentPeriodEnd)) {

            throw new IllegalStateException(
                    "subscription.expiration.too-early");
        }

        this.status =
                SubscriptionStatus.EXPIRED;

        this.cancelAtPeriodEnd = false;
        this.updatedAt = currentDate;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                || status ==
                SubscriptionStatus.CANCELLATION_SCHEDULED;
    }

    public boolean requiresRenewal(
            Instant currentDate) {

        validateTimestamp(currentDate);

        return status == SubscriptionStatus.ACTIVE
                && !cancelAtPeriodEnd
                && currentPeriodEnd != null
                && !currentDate.isBefore(
                currentPeriodEnd);
    }

    private static void validatePeriod(
            Instant periodStart,
            Instant periodEnd) {

        if (periodStart == null
                || periodEnd == null
                || !periodEnd.isAfter(
                periodStart)) {

            throw new IllegalArgumentException(
                    "subscription.period.invalid");
        }
    }

    private static void validateTimestamp(
            Instant timestamp) {

        if (timestamp == null) {
            throw new IllegalArgumentException(
                    "subscription.timestamp.invalid");
        }
    }

    public void setId(SubscriptionId id) {
        this.id = id;
    }

    public void setSubscriberId(
            SubscriberId subscriberId) {
        this.subscriberId = subscriberId;
    }

    public void setPlanId(PlanId planId) {
        this.planId = planId;
    }

    public void setStatus(
            SubscriptionStatus status) {
        this.status = status;
    }

    public void setCurrentPeriodStart(
            Instant currentPeriodStart) {
        this.currentPeriodStart =
                currentPeriodStart;
    }

    public void setCurrentPeriodEnd(
            Instant currentPeriodEnd) {
        this.currentPeriodEnd =
                currentPeriodEnd;
    }

    public void setCancelAtPeriodEnd(
            boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd =
                cancelAtPeriodEnd;
    }

    public void setCreatedAt(
            Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(
            Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}