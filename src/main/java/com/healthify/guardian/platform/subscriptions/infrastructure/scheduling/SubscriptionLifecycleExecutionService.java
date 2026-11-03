package com.healthify.guardian.platform.subscriptions.infrastructure.scheduling;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionLifecycleCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EvaluateSubscriptionLifecycleHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.SubscriptionCommandService;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.SubscriptionLifecycleAction;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.CancelSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ExpireSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated.SimulatedPaymentOutcome;
import com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated.SimulatedRenewalPaymentFlowService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class SubscriptionLifecycleExecutionService {

    private final EvaluateSubscriptionLifecycleHandler lifecycleHandler;
    private final SimulatedRenewalPaymentFlowService renewalPaymentFlowService;
    private final SubscriptionCommandService subscriptionCommandService;

    public SubscriptionLifecycleExecutionService(
            EvaluateSubscriptionLifecycleHandler lifecycleHandler,
            SimulatedRenewalPaymentFlowService renewalPaymentFlowService,
            SubscriptionCommandService subscriptionCommandService
    ) {
        this.lifecycleHandler =
                Objects.requireNonNull(lifecycleHandler);

        this.renewalPaymentFlowService =
                Objects.requireNonNull(renewalPaymentFlowService);

        this.subscriptionCommandService =
                Objects.requireNonNull(subscriptionCommandService);
    }

    public SubscriptionLifecycleExecutionResult execute(
            UUID subscriptionId,
            Instant evaluatedAt,
            SimulatedPaymentOutcome renewalOutcome
    ) {

        Objects.requireNonNull(
                subscriptionId,
                "subscriptionId must not be null"
        );

        Objects.requireNonNull(
                evaluatedAt,
                "evaluatedAt must not be null"
        );

        Objects.requireNonNull(
                renewalOutcome,
                "renewalOutcome must not be null"
        );

        SubscriptionLifecycleAction action =
                lifecycleHandler.handle(
                        new EvaluateSubscriptionLifecycleCommand(
                                subscriptionId,
                                evaluatedAt,
                                false
                        )
                );

        return switch (action) {

            case NONE ->
                    SubscriptionLifecycleExecutionResult.NONE;

            case CANCELLATION_DUE ->
                    executeCancellation(subscriptionId);

            case RENEWAL_DUE ->
                    executeRenewal(
                            subscriptionId,
                            evaluatedAt,
                            renewalOutcome
                    );

            case EXPIRATION_DUE ->
                    executeExpiration(subscriptionId);
        };
    }

    private SubscriptionLifecycleExecutionResult executeCancellation(
            UUID subscriptionId
    ) {

        subscriptionCommandService.handle(
                new CancelSubscriptionCommand(
                        subscriptionId
                )
        );

        return SubscriptionLifecycleExecutionResult.CANCELLED;
    }

    private SubscriptionLifecycleExecutionResult executeRenewal(
            UUID subscriptionId,
            Instant evaluatedAt,
            SimulatedPaymentOutcome renewalOutcome
    ) {

        if (renewalOutcome
                == SimulatedPaymentOutcome.CONFIRMED) {

            renewalPaymentFlowService.process(
                    subscriptionId,
                    SimulatedPaymentOutcome.CONFIRMED
            );

            return SubscriptionLifecycleExecutionResult.RENEWED;
        }

        renewalPaymentFlowService.process(
                subscriptionId,
                SimulatedPaymentOutcome.FAILED
        );

        SubscriptionLifecycleAction actionAfterFailure =
                lifecycleHandler.handle(
                        new EvaluateSubscriptionLifecycleCommand(
                                subscriptionId,
                                evaluatedAt,
                                true
                        )
                );

        if (actionAfterFailure
                == SubscriptionLifecycleAction.EXPIRATION_DUE) {

            return executeExpiration(
                    subscriptionId
            );
        }

        return SubscriptionLifecycleExecutionResult.RENEWAL_FAILED;
    }

    private SubscriptionLifecycleExecutionResult executeExpiration(
            UUID subscriptionId
    ) {

        subscriptionCommandService.handle(
                new ExpireSubscriptionCommand(
                        subscriptionId
                )
        );

        return SubscriptionLifecycleExecutionResult.EXPIRED;
    }
}