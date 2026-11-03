package com.healthify.guardian.platform.subscriptions.application.commandservices.impl;

import com.healthify.guardian.platform.subscriptions.application.commands.FailSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentFailedHandler;
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
public class PaymentFailedHandlerImpl
        implements PaymentFailedHandler {

    private final SubscriptionRepository subscriptionRepository;

    public PaymentFailedHandlerImpl(
            SubscriptionRepository subscriptionRepository
    ) {
        this.subscriptionRepository =
                Objects.requireNonNull(subscriptionRepository);
    }

    @Override
    public Subscription handle(
            FailSubscriptionPaymentCommand command
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
                    "Only a pending subscription can receive a payment failure"
            );
        }

        /*
         * No modificamos el aggregate.
         *
         * El pago falló, por lo que la solicitud de suscripción
         * permanece PENDING y puede reintentarse.
         */
        return subscription;
    }
}