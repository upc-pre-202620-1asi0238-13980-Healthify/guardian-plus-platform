package com.healthify.guardian.platform.subscriptions.application.commandservices.impl;

import com.healthify.guardian.platform.subscriptions.application.commands.InitiateSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.InitiateSubscriptionPaymentHandler;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentProvider;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentPurpose;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class InitiateSubscriptionPaymentHandlerImpl
        implements InitiateSubscriptionPaymentHandler {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentProvider paymentProvider;

    public InitiateSubscriptionPaymentHandlerImpl(
            SubscriptionRepository subscriptionRepository,
            PlanRepository planRepository,
            PaymentProvider paymentProvider
    ) {
        this.subscriptionRepository =
                Objects.requireNonNull(subscriptionRepository);

        this.planRepository =
                Objects.requireNonNull(planRepository);

        this.paymentProvider =
                Objects.requireNonNull(paymentProvider);
    }

    @Override
    public PaymentInitiationResult handle(
            InitiateSubscriptionPaymentCommand command
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

        validateSubscription(subscription);

        Plan plan = planRepository
                .findById(subscription.getPlanId())
                .orElseThrow(
                        () -> new SubscriptionPaymentException(
                                "Subscription plan not found"
                        )
                );

        validatePlan(plan);

        PaymentRequest paymentRequest =
                new PaymentRequest(
                        subscription.getId().value(),
                        subscription.getSubscriberId().value(),
                        subscription.getPlanId().value(),
                        plan.getPrice().amount(),
                        plan.getPrice().currency(),
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION
                );

        return paymentProvider.initiatePayment(paymentRequest);
    }

    private void validateSubscription(
            Subscription subscription
    ) {

        if (subscription.getStatus()
                != SubscriptionStatus.PENDING) {

            throw new SubscriptionPaymentException(
                    "Only a pending subscription can initiate payment"
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
                    "Subscription plan does not require payment"
            );
        }
    }
}