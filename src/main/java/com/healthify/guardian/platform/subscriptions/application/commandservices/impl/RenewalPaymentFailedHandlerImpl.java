package com.healthify.guardian.platform.subscriptions.application.commandservices.impl;

import com.healthify.guardian.platform.subscriptions.application.commands.FailRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class RenewalPaymentFailedHandlerImpl
        implements RenewalPaymentFailedHandler {

    private final SubscriptionRepository subscriptionRepository;

    public RenewalPaymentFailedHandlerImpl(
            SubscriptionRepository subscriptionRepository
    ) {
        this.subscriptionRepository =
                Objects.requireNonNull(subscriptionRepository);
    }

    @Override
    public Subscription handle(
            FailRenewalPaymentCommand command
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

            throw new SubscriptionPaymentException(
                    "Only an active subscription can receive a renewal payment failure"
            );
        }

        /*
         * No se modifica el aggregate.
         *
         * La renovación falló, así que el periodo actual
         * continúa exactamente igual.
         */
        return subscription;
    }
}