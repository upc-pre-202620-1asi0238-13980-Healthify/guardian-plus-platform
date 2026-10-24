package com.healthify.guardian.platform.subscriptions.application.commandservices.impl;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionExpirationCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionLifecycleCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionRenewalCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EvaluateSubscriptionExpirationHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EvaluateSubscriptionLifecycleHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EvaluateSubscriptionRenewalHandler;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.ExpirationEvaluation;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.RenewalEvaluation;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.SubscriptionLifecycleAction;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class EvaluateSubscriptionLifecycleHandlerImpl
        implements EvaluateSubscriptionLifecycleHandler {

    private final SubscriptionRepository subscriptionRepository;
    private final EvaluateSubscriptionRenewalHandler renewalHandler;
    private final EvaluateSubscriptionExpirationHandler expirationHandler;

    public EvaluateSubscriptionLifecycleHandlerImpl(
            SubscriptionRepository subscriptionRepository,
            EvaluateSubscriptionRenewalHandler renewalHandler,
            EvaluateSubscriptionExpirationHandler expirationHandler
    ) {
        this.subscriptionRepository =
                Objects.requireNonNull(subscriptionRepository);

        this.renewalHandler =
                Objects.requireNonNull(renewalHandler);

        this.expirationHandler =
                Objects.requireNonNull(expirationHandler);
    }

    @Override
    public SubscriptionLifecycleAction handle(
            EvaluateSubscriptionLifecycleCommand command
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

        if (subscription.getStatus()
                == SubscriptionStatus.CANCELLATION_SCHEDULED) {

            return evaluateScheduledCancellation(
                    subscription,
                    command.evaluatedAt()
            );
        }

        if (subscription.getStatus()
                != SubscriptionStatus.ACTIVE) {

            return SubscriptionLifecycleAction.NONE;
        }

        RenewalEvaluation renewalEvaluation =
                renewalHandler.handle(
                        new EvaluateSubscriptionRenewalCommand(
                                command.subscriptionId(),
                                command.evaluatedAt()
                        )
                );

        if (renewalEvaluation
                != RenewalEvaluation.DUE) {

            return SubscriptionLifecycleAction.NONE;
        }

        if (!command.renewalAttemptFailed()) {
            return SubscriptionLifecycleAction.RENEWAL_DUE;
        }

        ExpirationEvaluation expirationEvaluation =
                expirationHandler.handle(
                        new EvaluateSubscriptionExpirationCommand(
                                command.subscriptionId(),
                                command.evaluatedAt()
                        )
                );

        if (expirationEvaluation
                == ExpirationEvaluation.DUE) {

            return SubscriptionLifecycleAction.EXPIRATION_DUE;
        }

        return SubscriptionLifecycleAction.NONE;
    }

    private SubscriptionLifecycleAction evaluateScheduledCancellation(
            Subscription subscription,
            Instant evaluatedAt
    ) {

        Instant periodEnd =
                subscription.getCurrentPeriodEnd();

        if (periodEnd == null) {
            return SubscriptionLifecycleAction.NONE;
        }

        if (periodEnd.isAfter(evaluatedAt)) {
            return SubscriptionLifecycleAction.NONE;
        }

        return SubscriptionLifecycleAction.CANCELLATION_DUE;
    }
}