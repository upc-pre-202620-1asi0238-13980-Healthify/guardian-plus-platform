package com.healthify.guardian.platform.subscriptions.application.commandservices.impl;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;

@Service
@Transactional
public class RenewalPaymentConfirmedHandlerImpl
        implements RenewalPaymentConfirmedHandler {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    public RenewalPaymentConfirmedHandlerImpl(
            SubscriptionRepository subscriptionRepository,
            PlanRepository planRepository
    ) {
        this.subscriptionRepository =
                Objects.requireNonNull(subscriptionRepository);

        this.planRepository =
                Objects.requireNonNull(planRepository);
    }

    @Override
    public Subscription handle(
            ConfirmRenewalPaymentCommand command
    ) {

        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        Subscription subscription =
                subscriptionRepository
                        .findById(
                                new SubscriptionId(
                                        command.subscriptionId()
                                )
                        )
                        .orElseThrow(
                                () -> new SubscriptionPaymentException(
                                        "Subscription not found"
                                )
                        );

        validateSubscription(subscription);

        Plan plan =
                planRepository
                        .findById(subscription.getPlanId())
                        .orElseThrow(
                                () -> new SubscriptionPaymentException(
                                        "Subscription plan not found"
                                )
                        );

        validatePlan(plan);

        Instant newPeriodEnd =
                calculateNewPeriodEnd(
                        subscription.getCurrentPeriodEnd(),
                        plan.getBillingCycle()
                );

        Instant now =
                Instant.now();

        subscription.renew(
                newPeriodEnd,
                now
        );

        return subscriptionRepository.save(subscription);
    }

    private void validateSubscription(
            Subscription subscription
    ) {

        if (subscription.getStatus()
                != SubscriptionStatus.ACTIVE) {

            throw new SubscriptionPaymentException(
                    "Only an active subscription can be renewed"
            );
        }

        if (subscription.getCurrentPeriodEnd() == null) {
            throw new SubscriptionPaymentException(
                    "Active subscription must have a current period end"
            );
        }

        if (subscription.isCancelAtPeriodEnd()) {
            throw new SubscriptionPaymentException(
                    "Subscription scheduled for cancellation cannot be renewed"
            );
        }
    }

    private void validatePlan(
            Plan plan
    ) {

        if (!plan.isActive()) {
            throw new SubscriptionPaymentException(
                    "Subscription plan is not active"
            );
        }

        if (!plan.requiresPayment()) {
            throw new SubscriptionPaymentException(
                    "Subscription plan does not require renewal payment"
            );
        }
    }

    private Instant calculateNewPeriodEnd(
            Instant currentPeriodEnd,
            BillingCycle billingCycle
    ) {

        return switch (billingCycle) {

            case MONTHLY ->
                    currentPeriodEnd
                            .atZone(ZoneOffset.UTC)
                            .plusMonths(1)
                            .toInstant();

            case ANNUAL ->
                    currentPeriodEnd
                            .atZone(ZoneOffset.UTC)
                            .plusYears(1)
                            .toInstant();
        };
    }
}