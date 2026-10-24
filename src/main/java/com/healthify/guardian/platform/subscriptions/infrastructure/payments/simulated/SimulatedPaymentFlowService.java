package com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.InitiateSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.InitiateSubscriptionPaymentHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class SimulatedPaymentFlowService {

    private static final String SIMULATED_FAILURE_REASON =
            "Simulated payment declined";

    private final InitiateSubscriptionPaymentHandler initiatePaymentHandler;
    private final PaymentConfirmedHandler paymentConfirmedHandler;
    private final PaymentFailedHandler paymentFailedHandler;

    public SimulatedPaymentFlowService(
            InitiateSubscriptionPaymentHandler initiatePaymentHandler,
            PaymentConfirmedHandler paymentConfirmedHandler,
            PaymentFailedHandler paymentFailedHandler
    ) {
        this.initiatePaymentHandler =
                Objects.requireNonNull(initiatePaymentHandler);

        this.paymentConfirmedHandler =
                Objects.requireNonNull(paymentConfirmedHandler);

        this.paymentFailedHandler =
                Objects.requireNonNull(paymentFailedHandler);
    }

    public SimulatedPaymentFlowResult process(
            UUID subscriptionId,
            SimulatedPaymentOutcome outcome
    ) {

        Objects.requireNonNull(
                subscriptionId,
                "subscriptionId must not be null"
        );

        Objects.requireNonNull(
                outcome,
                "outcome must not be null"
        );

        PaymentInitiationResult payment =
                initiatePaymentHandler.handle(
                        new InitiateSubscriptionPaymentCommand(
                                subscriptionId
                        )
                );

        if (payment.status() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Simulated payment must start in PENDING status"
            );
        }

        Subscription subscription =
                switch (outcome) {

                    case CONFIRMED ->
                            paymentConfirmedHandler.handle(
                                    new ConfirmSubscriptionPaymentCommand(
                                            subscriptionId,
                                            payment.externalReference()
                                    )
                            );

                    case FAILED ->
                            paymentFailedHandler.handle(
                                    new FailSubscriptionPaymentCommand(
                                            subscriptionId,
                                            payment.externalReference(),
                                            SIMULATED_FAILURE_REASON
                                    )
                            );
                };

        return new SimulatedPaymentFlowResult(
                payment,
                outcome,
                subscription
        );
    }
}