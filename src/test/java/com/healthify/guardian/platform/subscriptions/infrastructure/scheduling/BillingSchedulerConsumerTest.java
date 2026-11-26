
package com.healthify.guardian.platform.subscriptions.infrastructure.scheduling;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.SubscriptionLifecycleCandidateFinder;
import com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated.SimulatedPaymentOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BillingSchedulerConsumerTest {

    private SubscriptionLifecycleCandidateFinder candidateFinder;
    private SubscriptionLifecycleExecutionService executionService;

    private BillingSchedulerConsumer consumer;

    @BeforeEach
    void setUp() {

        candidateFinder =
                mock(SubscriptionLifecycleCandidateFinder.class);

        executionService =
                mock(SubscriptionLifecycleExecutionService.class);

        consumer =
                new BillingSchedulerConsumer(
                        candidateFinder,
                        executionService,
                        "CONFIRMED"
                );
    }

    @Test
    void shouldDoNothingWhenThereAreNoDueSubscriptions() {

        when(candidateFinder.findDueSubscriptionIds(
                any(Instant.class)
        )).thenReturn(List.of());

        consumer.evaluateDueSubscriptions();

        verifyNoInteractions(executionService);
    }

    @Test
    void shouldEvaluateEveryDueSubscription() {

        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        when(candidateFinder.findDueSubscriptionIds(
                any(Instant.class)
        )).thenReturn(List.of(first, second));

        when(executionService.execute(
                any(UUID.class),
                any(Instant.class),
                eq(SimulatedPaymentOutcome.CONFIRMED)
        )).thenReturn(
                SubscriptionLifecycleExecutionResult.RENEWED
        );

        consumer.evaluateDueSubscriptions();

        verify(executionService, times(2)).execute(
                any(UUID.class),
                any(Instant.class),
                eq(SimulatedPaymentOutcome.CONFIRMED)
        );
    }

    @Test
    void shouldContinueWhenOneSubscriptionFails() {

        UUID failingSubscription = UUID.randomUUID();
        UUID successfulSubscription = UUID.randomUUID();

        when(candidateFinder.findDueSubscriptionIds(
                any(Instant.class)
        )).thenReturn(
                List.of(
                        failingSubscription,
                        successfulSubscription
                )
        );

        when(executionService.execute(
                eq(failingSubscription),
                any(Instant.class),
                eq(SimulatedPaymentOutcome.CONFIRMED)
        )).thenThrow(
                new RuntimeException(
                        "Simulated lifecycle failure"
                )
        );

        when(executionService.execute(
                eq(successfulSubscription),
                any(Instant.class),
                eq(SimulatedPaymentOutcome.CONFIRMED)
        )).thenReturn(
                SubscriptionLifecycleExecutionResult.RENEWED
        );

        consumer.evaluateDueSubscriptions();

        verify(executionService).execute(
                eq(successfulSubscription),
                any(Instant.class),
                eq(SimulatedPaymentOutcome.CONFIRMED)
        );
    }

    private ApplicationContextRunner contextRunner() {

        return new ApplicationContextRunner()
                .withUserConfiguration(
                        BillingSchedulerConsumer.class
                )
                .withBean(
                        SubscriptionLifecycleCandidateFinder.class,
                        () -> candidateFinder
                )
                .withBean(
                        SubscriptionLifecycleExecutionService.class,
                        () -> executionService
                );
    }

    @Test
    void shouldAllowEnabledSchedulerWithSimulatedProvider() {

        contextRunner()
                .withPropertyValues(
                        "subscriptions.billing.scheduler.enabled=true",
                        "subscriptions.billing.payment-provider=simulated"
                )
                .run(context -> {

                    assertThat(context).hasNotFailed();

                    assertThat(context)
                            .hasSingleBean(
                                    BillingSchedulerConsumer.class
                            );
                });
    }

    @Test
    void shouldRejectEnabledSchedulerWithStripeProvider() {

        contextRunner()
                .withPropertyValues(
                        "subscriptions.billing.scheduler.enabled=true",
                        "subscriptions.billing.payment-provider=stripe"
                )
                .run(context -> {

                    assertThat(context).hasFailed();

                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(
                                    IllegalStateException.class
                            )
                            .hasRootCauseMessage(
                                    "Billing scheduler currently supports only the simulated payment provider"
                            );

                    verifyNoInteractions(executionService);
                });
    }

    @Test
    void shouldAllowStripeProviderWhenSchedulerIsDisabled() {

        contextRunner()
                .withPropertyValues(
                        "subscriptions.billing.scheduler.enabled=false",
                        "subscriptions.billing.payment-provider=stripe"
                )
                .run(context -> {

                    assertThat(context).hasNotFailed();

                    assertThat(context)
                            .doesNotHaveBean(
                                    BillingSchedulerConsumer.class
                            );
                });
    }
}
