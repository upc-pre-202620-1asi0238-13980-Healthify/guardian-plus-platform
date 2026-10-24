package com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.InitiateRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.InitiateRenewalPaymentHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class SimulatedRenewalPaymentFlowService {

    private static final String SIMULATED_FAILURE_REASON =
            "Simulated renewal payment declined";

    private final InitiateRenewalPaymentHandler initiateRenewalPaymentHandler;
    private final RenewalPaymentConfirmedHandler renewalPaymentConfirmedHandler;
    private final RenewalPaymentFailedHandler renewalPaymentFailedHandler;

    public SimulatedRenewalPaymentFlowService(
            InitiateRenewalPaymentHandler initiateRenewalPaymentHandler,
            RenewalPaymentConfirmedHandler renewalPaymentConfirmedHandler,
            RenewalPaymentFailedHandler renewalPaymentFailedHandler
    ) {
        this.initiateRenewalPaymentHandler =
                Objects.requireNonNull(initiateRenewalPaymentHandler);

        this.renewalPaymentConfirmedHandler =
                Objects.requireNonNull(renewalPaymentConfirmedHandler);

        this.renewalPaymentFailedHandler =
                Objects.requireNonNull(renewalPaymentFailedHandler);
    }

    public SimulatedRenewalPaymentFlowResult process(
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
                initiateRenewalPaymentHandler.handle(
                        new InitiateRenewalPaymentCommand(
                                subscriptionId
                        )
                );

        if (payment.status() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Simulated renewal payment must start in PENDING status"
            );
        }

        Subscription subscription =
                switch (outcome) {

                    case CONFIRMED ->
                            renewalPaymentConfirmedHandler.handle(
                                    new ConfirmRenewalPaymentCommand(
                                            subscriptionId,
                                            payment.externalReference()
                                    )
                            );

                    case FAILED ->
                            renewalPaymentFailedHandler.handle(
                                    new FailRenewalPaymentCommand(
                                            subscriptionId,
                                            payment.externalReference(),
                                            SIMULATED_FAILURE_REASON
                                    )
                            );
                };

        return new SimulatedRenewalPaymentFlowResult(
                payment,
                outcome,
                subscription
        );
    }
}