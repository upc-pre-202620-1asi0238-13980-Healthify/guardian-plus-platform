package com.healthify.guardian.platform.subscriptions.application.commandservices.impl;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionRenewalCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EvaluateSubscriptionRenewalHandler;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.RenewalEvaluation;
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
public class EvaluateSubscriptionRenewalHandlerImpl
        implements EvaluateSubscriptionRenewalHandler {

    private final SubscriptionRepository subscriptionRepository;

    public EvaluateSubscriptionRenewalHandlerImpl(
            SubscriptionRepository subscriptionRepository
    ) {
        this.subscriptionRepository =
                Objects.requireNonNull(subscriptionRepository);
    }

    @Override
    public RenewalEvaluation handle(
            EvaluateSubscriptionRenewalCommand command
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
                != SubscriptionStatus.ACTIVE) {

            return RenewalEvaluation.NOT_ELIGIBLE;
        }

        if (subscription.isCancelAtPeriodEnd()) {
            return RenewalEvaluation.NOT_ELIGIBLE;
        }

        Instant periodEnd =
                subscription.getCurrentPeriodEnd();

        if (periodEnd == null) {
            return RenewalEvaluation.NOT_ELIGIBLE;
        }

        if (periodEnd.isAfter(command.evaluatedAt())) {
            return RenewalEvaluation.NOT_DUE;
        }

        return RenewalEvaluation.DUE;
    }
}