package com.healthify.guardian.platform.subscriptions.infrastructure.payments.stripe;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.ProcessedPaymentWebhookEventPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.ProcessedPaymentWebhookEventPersistenceRepository;
import com.stripe.Stripe;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StripeWebhookServiceTest {

    private static final String WEBHOOK_SECRET =
            "guardian-plus-test-webhook-secret";

    private PaymentConfirmedHandler
            paymentConfirmedHandler;

    private PaymentFailedHandler
            paymentFailedHandler;

    private RenewalPaymentConfirmedHandler
            renewalPaymentConfirmedHandler;

    private RenewalPaymentFailedHandler
            renewalPaymentFailedHandler;

    private ProcessedPaymentWebhookEventPersistenceRepository
            processedEventRepository;

    private StripeWebhookService service;

    @BeforeEach
    void setUp() {

        paymentConfirmedHandler =
                mock(PaymentConfirmedHandler.class);

        paymentFailedHandler =
                mock(PaymentFailedHandler.class);

        renewalPaymentConfirmedHandler =
                mock(RenewalPaymentConfirmedHandler.class);

        renewalPaymentFailedHandler =
                mock(RenewalPaymentFailedHandler.class);

        processedEventRepository =
                mock(
                        ProcessedPaymentWebhookEventPersistenceRepository.class
                );

        service =
                new StripeWebhookService(
                        WEBHOOK_SECRET,
                        paymentConfirmedHandler,
                        paymentFailedHandler,
                        renewalPaymentConfirmedHandler,
                        renewalPaymentFailedHandler,
                        processedEventRepository
                );
    }

    @Test
    void shouldConfirmSubscriptionActivationPayment()
            throws Exception {

        UUID subscriptionId =
                UUID.randomUUID();

        String paymentIntentId =
                "pi_test_activation";

        String payload =
                createPayload(
                        "evt_test_activation",
                        "payment_intent.succeeded",
                        paymentIntentId,
                        subscriptionId,
                        "SUBSCRIPTION_ACTIVATION",
                        null
                );

        String signature =
                Webhook.Signature.generateSignatureHeader(
                        payload,
                        WEBHOOK_SECRET
                );

        when(
                processedEventRepository.existsById(
                        "payment_intent.succeeded:"
                                + paymentIntentId
                )
        ).thenReturn(false);

        service.process(
                payload,
                signature
        );

        ArgumentCaptor<ConfirmSubscriptionPaymentCommand>
                commandCaptor =
                ArgumentCaptor.forClass(
                        ConfirmSubscriptionPaymentCommand.class
                );

        verify(paymentConfirmedHandler)
                .handle(
                        commandCaptor.capture()
                );

        assertEquals(
                subscriptionId,
                commandCaptor
                        .getValue()
                        .subscriptionId()
        );

        assertEquals(
                paymentIntentId,
                commandCaptor
                        .getValue()
                        .externalReference()
        );

        verify(processedEventRepository)
                .save(
                        any(
                                ProcessedPaymentWebhookEventPersistenceEntity.class
                        )
                );

        verifyNoInteractions(
                paymentFailedHandler,
                renewalPaymentConfirmedHandler,
                renewalPaymentFailedHandler
        );
    }

    @Test
    void shouldConfirmSubscriptionRenewalPayment()
            throws Exception {

        UUID subscriptionId =
                UUID.randomUUID();

        String paymentIntentId =
                "pi_test_renewal";

        String payload =
                createPayload(
                        "evt_test_renewal",
                        "payment_intent.succeeded",
                        paymentIntentId,
                        subscriptionId,
                        "SUBSCRIPTION_RENEWAL",
                        null
                );

        String signature =
                Webhook.Signature.generateSignatureHeader(
                        payload,
                        WEBHOOK_SECRET
                );

        when(
                processedEventRepository.existsById(
                        "payment_intent.succeeded:"
                                + paymentIntentId
                )
        ).thenReturn(false);

        service.process(
                payload,
                signature
        );

        ArgumentCaptor<ConfirmRenewalPaymentCommand>
                commandCaptor =
                ArgumentCaptor.forClass(
                        ConfirmRenewalPaymentCommand.class
                );

        verify(renewalPaymentConfirmedHandler)
                .handle(
                        commandCaptor.capture()
                );

        assertEquals(
                subscriptionId,
                commandCaptor
                        .getValue()
                        .subscriptionId()
        );

        assertEquals(
                paymentIntentId,
                commandCaptor
                        .getValue()
                        .externalReference()
        );

        verify(processedEventRepository)
                .save(
                        any(
                                ProcessedPaymentWebhookEventPersistenceEntity.class
                        )
                );

        verifyNoInteractions(
                paymentConfirmedHandler,
                paymentFailedHandler,
                renewalPaymentFailedHandler
        );
    }

    @Test
    void shouldHandleSubscriptionActivationPaymentFailure()
            throws Exception {

        UUID subscriptionId =
                UUID.randomUUID();

        String paymentIntentId =
                "pi_test_failure";

        String payload =
                createPayload(
                        "evt_test_failure",
                        "payment_intent.payment_failed",
                        paymentIntentId,
                        subscriptionId,
                        "SUBSCRIPTION_ACTIVATION",
                        "Your card was declined."
                );

        String signature =
                Webhook.Signature.generateSignatureHeader(
                        payload,
                        WEBHOOK_SECRET
                );

        when(
                processedEventRepository.existsById(
                        "payment_intent.payment_failed:"
                                + paymentIntentId
                )
        ).thenReturn(false);

        service.process(
                payload,
                signature
        );

        ArgumentCaptor<FailSubscriptionPaymentCommand>
                commandCaptor =
                ArgumentCaptor.forClass(
                        FailSubscriptionPaymentCommand.class
                );

        verify(paymentFailedHandler)
                .handle(
                        commandCaptor.capture()
                );

        assertEquals(
                subscriptionId,
                commandCaptor
                        .getValue()
                        .subscriptionId()
        );

        assertEquals(
                paymentIntentId,
                commandCaptor
                        .getValue()
                        .externalReference()
        );

        assertEquals(
                "Your card was declined.",
                commandCaptor
                        .getValue()
                        .reason()
        );

        verify(processedEventRepository)
                .save(
                        any(
                                ProcessedPaymentWebhookEventPersistenceEntity.class
                        )
                );

        verifyNoInteractions(
                paymentConfirmedHandler,
                renewalPaymentConfirmedHandler,
                renewalPaymentFailedHandler
        );
    }

    @Test
    void shouldIgnoreAlreadyProcessedPaymentEvent()
            throws Exception {

        UUID subscriptionId =
                UUID.randomUUID();

        String paymentIntentId =
                "pi_test_duplicate";

        String eventKey =
                "payment_intent.succeeded:"
                        + paymentIntentId;

        String payload =
                createPayload(
                        "evt_test_duplicate",
                        "payment_intent.succeeded",
                        paymentIntentId,
                        subscriptionId,
                        "SUBSCRIPTION_RENEWAL",
                        null
                );

        String signature =
                Webhook.Signature.generateSignatureHeader(
                        payload,
                        WEBHOOK_SECRET
                );

        when(
                processedEventRepository.existsById(
                        eventKey
                )
        ).thenReturn(true);

        service.process(
                payload,
                signature
        );

        verify(processedEventRepository)
                .existsById(
                        eventKey
                );

        verify(
                processedEventRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                paymentConfirmedHandler,
                paymentFailedHandler,
                renewalPaymentConfirmedHandler,
                renewalPaymentFailedHandler
        );
    }

    private String createPayload(
            String eventId,
            String eventType,
            String paymentIntentId,
            UUID subscriptionId,
            String purpose,
            String failureMessage
    ) {

        String lastPaymentError =
                failureMessage == null
                        ? "null"
                        : """
                        {
                          "message": "%s"
                        }
                        """.formatted(
                                failureMessage
                        );

        return """
                {
                  "id": "%s",
                  "object": "event",
                  "api_version": "%s",
                  "type": "%s",
                  "data": {
                    "object": {
                      "id": "%s",
                      "object": "payment_intent",
                      "metadata": {
                        "subscriptionId": "%s",
                        "purpose": "%s"
                      },
                      "last_payment_error": %s
                    }
                  }
                }
                """.formatted(
                        eventId,
                        Stripe.API_VERSION,
                        eventType,
                        paymentIntentId,
                        subscriptionId,
                        purpose,
                        lastPaymentError
                );
    }
}