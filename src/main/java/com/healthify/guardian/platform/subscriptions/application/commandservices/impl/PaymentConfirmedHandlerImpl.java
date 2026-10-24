package com.healthify.guardian.platform.subscriptions.application.commandservices.impl;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EntitlementSetCommandService;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.SyncEntitlementsFromPlanCommand;
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
public class PaymentConfirmedHandlerImpl
        implements PaymentConfirmedHandler {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final EntitlementSetCommandService entitlementSetCommandService;

    public PaymentConfirmedHandlerImpl(
            SubscriptionRepository subscriptionRepository,
            PlanRepository planRepository,
            EntitlementSetCommandService entitlementSetCommandService
    ) {
        this.subscriptionRepository =
                Objects.requireNonNull(subscriptionRepository);

        this.planRepository =
                Objects.requireNonNull(planRepository);

        this.entitlementSetCommandService =
                Objects.requireNonNull(entitlementSetCommandService);
    }

    @Override
    public Subscription handle(
            ConfirmSubscriptionPaymentCommand command
    ) {

        Objects.requireNonNull(command, "command must not be null");

        Subscription subscription = subscriptionRepository
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

        if (subscription.getStatus()
                != SubscriptionStatus.PENDING) {

            throw new SubscriptionPaymentException(
                    "Only a pending subscription can be activated by payment"
            );
        }

        Plan plan = planRepository
                .findById(subscription.getPlanId())
                .orElseThrow(
                        () -> new SubscriptionPaymentException(
                                "Subscription plan not found"
                        )
                );

        if (!plan.isActive()) {
            throw new SubscriptionPaymentException(
                    "Subscription plan is not active"
            );
        }

        if (!plan.requiresPayment()) {
            throw new SubscriptionPaymentException(
                    "Subscription plan does not require payment"
            );
        }

        Instant now = Instant.now();

        Instant periodEnd =
                calculatePeriodEnd(
                        now,
                        plan.getBillingCycle()
                );

        subscription.activate(
                subscription.getPlanId(),
                now,
                periodEnd,
                now
        );

        entitlementSetCommandService.handle(
                new SyncEntitlementsFromPlanCommand(
                        subscription.getId().value(),
                        subscription.getPlanId().value()
                )
        );

        return subscriptionRepository.save(subscription);
    }

    private Instant calculatePeriodEnd(
            Instant periodStart,
            BillingCycle billingCycle
    ) {

        return switch (billingCycle) {

            case MONTHLY ->
                    periodStart
                            .atZone(ZoneOffset.UTC)
                            .plusMonths(1)
                            .toInstant();

            case ANNUAL ->
                    periodStart
                            .atZone(ZoneOffset.UTC)
                            .plusYears(1)
                            .toInstant();
        };
    }
}