package com.healthify.guardian.platform.subscriptions.application.commandservices.impl;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionExpirationCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EvaluateSubscriptionExpirationHandler;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.ExpirationEvaluation;
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
public class EvaluateSubscriptionExpirationHandlerImpl
        implements EvaluateSubscriptionExpirationHandler {

    private final SubscriptionRepository subscriptionRepository;

    public EvaluateSubscriptionExpirationHandlerImpl(
            SubscriptionRepository subscriptionRepository
    ) {
        this.subscriptionRepository =
                Objects.requireNonNull(subscriptionRepository);
    }

    @Override
    public ExpirationEvaluation handle(
            EvaluateSubscriptionExpirationCommand command
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

        /*
         * CANCELLATION_SCHEDULED se procesa mediante cancel(),
         * no mediante expire().
         */
        if (subscription.getStatus()
                != SubscriptionStatus.ACTIVE) {

            return ExpirationEvaluation.NOT_ELIGIBLE;
        }

        Instant periodEnd =
                subscription.getCurrentPeriodEnd();

        if (periodEnd == null) {
            return ExpirationEvaluation.NOT_ELIGIBLE;
        }

        if (periodEnd.isAfter(command.evaluatedAt())) {
            return ExpirationEvaluation.NOT_DUE;
        }

        return ExpirationEvaluation.DUE;
    }
}