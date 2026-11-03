package com.healthify.guardian.platform.subscriptions.domain.model.aggregates;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionTest {

    private static final SubscriberId SUBSCRIBER_ID =
            new SubscriberId(
                    UUID.fromString(
                            "11111111-1111-1111-1111-111111111111"));

    private static final PlanId PLAN_ID =
            new PlanId(
                    UUID.fromString(
                            "22222222-2222-2222-2222-222222222222"));

    private static final Instant CREATED_AT =
            Instant.parse(
                    "2026-10-23T12:00:00Z");

    @Test
    void shouldCreatePendingSubscription() {

        var subscription =
                new Subscription(
                        SUBSCRIBER_ID,
                        PLAN_ID,
                        CREATED_AT);

        assertEquals(
                SubscriptionStatus.PENDING,
                subscription.getStatus());

        assertEquals(
                SUBSCRIBER_ID,
                subscription.getSubscriberId());

        assertEquals(
                PLAN_ID,
                subscription.getPlanId());

        assertFalse(
                subscription.isCancelAtPeriodEnd());
    }

    @Test
    void shouldActivatePendingSubscription() {

        var subscription =
                new Subscription(
                        SUBSCRIBER_ID,
                        PLAN_ID,
                        CREATED_AT);

        var periodStart =
                Instant.parse(
                        "2026-10-23T12:00:00Z");

        var periodEnd =
                Instant.parse(
                        "2026-11-23T12:00:00Z");

        subscription.activate(
                PLAN_ID,
                periodStart,
                periodEnd,
                periodStart);

        assertEquals(
                SubscriptionStatus.ACTIVE,
                subscription.getStatus());

        assertTrue(
                subscription.isActive());

        assertEquals(
                periodEnd,
                subscription.getCurrentPeriodEnd());
    }

    @Test
    void shouldScheduleCancellation() {

        var subscription =
                activeSubscription();

        subscription.requestCancellation(
                Instant.parse(
                        "2026-10-24T12:00:00Z"));

        assertEquals(
                SubscriptionStatus.CANCELLATION_SCHEDULED,
                subscription.getStatus());

        assertTrue(
                subscription.isCancelAtPeriodEnd());

        assertTrue(
                subscription.isActive());
    }

    @Test
    void shouldRenewActiveSubscription() {

        var subscription =
                activeSubscription();

        var newEnd =
                Instant.parse(
                        "2026-12-23T12:00:00Z");

        subscription.renew(
                newEnd,
                Instant.parse(
                        "2026-11-23T12:00:00Z"));

        assertEquals(
                newEnd,
                subscription.getCurrentPeriodEnd());

        assertEquals(
                Instant.parse(
                        "2026-11-23T12:00:00Z"),
                subscription.getCurrentPeriodStart());
    }

    @Test
    void shouldRejectRenewalWhenCancellationIsScheduled() {

        var subscription =
                activeSubscription();

        subscription.requestCancellation(
                Instant.parse(
                        "2026-10-24T12:00:00Z"));

        assertThrows(
                IllegalStateException.class,
                () -> subscription.renew(
                        Instant.parse(
                                "2026-12-23T12:00:00Z"),
                        Instant.parse(
                                "2026-11-23T12:00:00Z")));
    }

    private Subscription activeSubscription() {

        var subscription =
                new Subscription(
                        SUBSCRIBER_ID,
                        PLAN_ID,
                        CREATED_AT);

        subscription.activate(
                PLAN_ID,
                Instant.parse(
                        "2026-10-23T12:00:00Z"),
                Instant.parse(
                        "2026-11-23T12:00:00Z"),
                CREATED_AT);

        return subscription;
    }
}