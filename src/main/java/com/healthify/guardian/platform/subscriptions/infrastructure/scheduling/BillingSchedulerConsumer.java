package com.healthify.guardian.platform.subscriptions.infrastructure.scheduling;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.SubscriptionLifecycleCandidateFinder;
import com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated.SimulatedPaymentOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "subscriptions.billing.scheduler.enabled",
        havingValue = "true"
)
public class BillingSchedulerConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    BillingSchedulerConsumer.class
            );

    private final SubscriptionLifecycleCandidateFinder candidateFinder;
    private final SubscriptionLifecycleExecutionService lifecycleExecutionService;
    private final SimulatedPaymentOutcome renewalOutcome;

    public BillingSchedulerConsumer(
            SubscriptionLifecycleCandidateFinder candidateFinder,
            SubscriptionLifecycleExecutionService lifecycleExecutionService,
            @Value(
                    "${subscriptions.billing.simulated-renewal-outcome:CONFIRMED}"
            )
            String configuredRenewalOutcome
    ) {

        this.candidateFinder =
                Objects.requireNonNull(candidateFinder);

        this.lifecycleExecutionService =
                Objects.requireNonNull(
                        lifecycleExecutionService
                );

        this.renewalOutcome =
                parseOutcome(
                        configuredRenewalOutcome
                );
    }

    @Scheduled(
            fixedDelayString =
                    "${subscriptions.billing.scheduler.delay-ms:60000}"
    )
    public void evaluateDueSubscriptions() {

        Instant evaluatedAt =
                Instant.now();

        List<UUID> subscriptionIds =
                candidateFinder.findDueSubscriptionIds(
                        evaluatedAt
                );

        for (UUID subscriptionId : subscriptionIds) {

            try {

                SubscriptionLifecycleExecutionResult result =
                        lifecycleExecutionService.execute(
                                subscriptionId,
                                evaluatedAt,
                                renewalOutcome
                        );

                LOGGER.info(
                        "Subscription lifecycle evaluated. subscriptionId={}, result={}",
                        subscriptionId,
                        result
                );

            } catch (RuntimeException exception) {

                /*
                 * Una suscripción con error no debe impedir
                 * evaluar las demás.
                 */
                LOGGER.error(
                        "Subscription lifecycle evaluation failed. subscriptionId={}",
                        subscriptionId,
                        exception
                );
            }
        }
    }

    private SimulatedPaymentOutcome parseOutcome(
            String configuredOutcome
    ) {

        Objects.requireNonNull(
                configuredOutcome,
                "configuredRenewalOutcome must not be null"
        );

        try {

            return SimulatedPaymentOutcome.valueOf(
                    configuredOutcome
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Invalid simulated renewal outcome: "
                            + configuredOutcome,
                    exception
            );
        }
    }
}