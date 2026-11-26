package com.healthify.guardian.platform.subscriptions.infrastructure.payments.stripe;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentPurpose;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PaymentOperationPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.ProcessedPaymentWebhookEventPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.PaymentOperationPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.ProcessedPaymentWebhookEventPersistenceRepository;
import com.stripe.Stripe;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StripeWebhookServiceTest {

    private static final String WEBHOOK_SECRET =
            "guardian-plus-test-webhook-secret";

    private static final String PAYMENT_SUCCEEDED =
            "payment_intent.succeeded";

    private static final String PAYMENT_FAILED =
            "payment_intent.payment_failed";

    private static final String PAYMENT_INTENT_ID =
            "pi_test_payment";

    private static final Instant PERIOD_END =
            Instant.parse("2026-12-21T10:00:00Z");

    private PaymentConfirmedHandler paymentConfirmedHandler;

    private PaymentFailedHandler paymentFailedHandler;

    private RenewalPaymentConfirmedHandler
            renewalPaymentConfirmedHandler;

    private RenewalPaymentFailedHandler
            renewalPaymentFailedHandler;

    private ProcessedPaymentWebhookEventPersistenceRepository
            processedEventRepository;

    private PaymentOperationPersistenceRepository
            paymentOperationRepository;

    private SubscriptionRepository subscriptionRepository;

    private StripeWebhookService service;

    private UUID subscriptionId;
    private UUID subscriberId;
    private UUID planId;

    private Subscription subscription;

    @BeforeEach
    void setUp() {

        subscriptionId = UUID.randomUUID();
        subscriberId = UUID.randomUUID();
        planId = UUID.randomUUID();

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

        paymentOperationRepository =
                mock(
                        PaymentOperationPersistenceRepository.class
                );

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        subscription =
                mock(Subscription.class);

        service =
                new StripeWebhookService(
                        WEBHOOK_SECRET,
                        paymentConfirmedHandler,
                        paymentFailedHandler,
                        renewalPaymentConfirmedHandler,
                        renewalPaymentFailedHandler,
                        processedEventRepository,
                        paymentOperationRepository,
                        subscriptionRepository
                );
    }

    @Test
    void shouldConfirmSubscriptionActivationPayment()
            throws Exception {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        process(
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                )
        );

        ArgumentCaptor<ConfirmSubscriptionPaymentCommand>
                commandCaptor =
                ArgumentCaptor.forClass(
                        ConfirmSubscriptionPaymentCommand.class
                );

        verify(paymentConfirmedHandler)
                .handle(commandCaptor.capture());

        assertEquals(
                subscriptionId,
                commandCaptor.getValue().subscriptionId()
        );

        assertEquals(
                PAYMENT_INTENT_ID,
                commandCaptor.getValue().externalReference()
        );

        verify(subscriptionRepository)
                .acquireSubscriberLock(
                        new SubscriberId(subscriberId)
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

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_RENEWAL,
                PERIOD_END
        );

        process(
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_RENEWAL,
                        PERIOD_END,
                        null
                )
        );

        ArgumentCaptor<ConfirmRenewalPaymentCommand>
                commandCaptor =
                ArgumentCaptor.forClass(
                        ConfirmRenewalPaymentCommand.class
                );

        verify(renewalPaymentConfirmedHandler)
                .handle(commandCaptor.capture());

        assertEquals(
                subscriptionId,
                commandCaptor.getValue().subscriptionId()
        );

        assertEquals(
                PAYMENT_INTENT_ID,
                commandCaptor.getValue().externalReference()
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

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        process(
                createPayload(
                        PAYMENT_FAILED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        "Your card was declined."
                )
        );

        ArgumentCaptor<FailSubscriptionPaymentCommand>
                commandCaptor =
                ArgumentCaptor.forClass(
                        FailSubscriptionPaymentCommand.class
                );

        verify(paymentFailedHandler)
                .handle(commandCaptor.capture());

        assertEquals(
                subscriptionId,
                commandCaptor.getValue().subscriptionId()
        );

        assertEquals(
                PAYMENT_INTENT_ID,
                commandCaptor.getValue().externalReference()
        );

        assertEquals(
                "Your card was declined.",
                commandCaptor.getValue().reason()
        );

        verify(processedEventRepository)
                .save(any());

        verifyNoInteractions(
                paymentConfirmedHandler,
                renewalPaymentConfirmedHandler,
                renewalPaymentFailedHandler
        );
    }

    @Test
    void shouldIgnoreAlreadyProcessedPaymentEvent()
            throws Exception {

        when(
                processedEventRepository.existsById(
                        eventKey(PAYMENT_SUCCEEDED)
                )
        ).thenReturn(true);

        process(
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_RENEWAL,
                        PERIOD_END,
                        null
                )
        );

        verify(processedEventRepository)
                .existsById(
                        eventKey(PAYMENT_SUCCEEDED)
                );

        verifyNoInteractions(
                paymentOperationRepository,
                subscriptionRepository
        );

        verifyRejectedPayment();
    }

    @Test
    void shouldHandleRenewalPaymentFailure()
            throws Exception {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_RENEWAL,
                PERIOD_END
        );

        process(
                createPayload(
                        PAYMENT_FAILED,
                        PaymentPurpose.SUBSCRIPTION_RENEWAL,
                        PERIOD_END,
                        "Your card was declined."
                )
        );

        verify(renewalPaymentFailedHandler)
                .handle(
                        any(FailRenewalPaymentCommand.class)
                );

        verify(processedEventRepository)
                .save(any());

        verifyNoInteractions(
                paymentConfirmedHandler,
                paymentFailedHandler,
                renewalPaymentConfirmedHandler
        );
    }

    @Test
    void shouldIgnoreEventProcessedWhileWaitingForLock()
            throws Exception {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        when(
                processedEventRepository.existsById(
                        eventKey(PAYMENT_SUCCEEDED)
                )
        ).thenReturn(false, true);

        process(
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                )
        );

        verify(subscriptionRepository)
                .acquireSubscriberLock(
                        new SubscriberId(subscriberId)
                );

        verify(paymentOperationRepository)
                .findByIdForUpdate(
                        operationKey(
                                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                                null
                        )
                );

        verify(processedEventRepository, times(2))
                .existsById(
                        eventKey(PAYMENT_SUCCEEDED)
                );

        verifyRejectedPayment();
    }

    @Test
    void shouldRejectPaymentWithIncorrectAmount() {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        String payload =
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                ).replace(
                        "\"amount\": 2990",
                        "\"amount\": 100"
                );

        assertThrows(
                IllegalStateException.class,
                () -> process(payload)
        );

        verifyRejectedPayment();
    }

    @Test
    void shouldRejectPaymentWithIncorrectCurrency() {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        String payload =
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                ).replace(
                        "\"currency\": \"pen\"",
                        "\"currency\": \"usd\""
                );

        assertThrows(
                IllegalStateException.class,
                () -> process(payload)
        );

        verifyRejectedPayment();
    }

    @Test
    void shouldRejectPaymentWithIncorrectReceivedAmount() {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        String payload =
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                ).replace(
                        "\"amount_received\": 2990",
                        "\"amount_received\": 100"
                );

        assertThrows(
                IllegalStateException.class,
                () -> process(payload)
        );

        verifyRejectedPayment();
    }

    @Test
    void shouldRejectPaymentWithIncorrectPlanMetadata() {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        String payload =
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                ).replace(
                        planId.toString(),
                        UUID.randomUUID().toString()
                );

        assertThrows(
                IllegalStateException.class,
                () -> process(payload)
        );

        verifyRejectedPayment();
    }

    @Test
    void shouldRejectPaymentWithoutRegisteredOperation() {

        when(
                paymentOperationRepository.findByIdForUpdate(
                        operationKey(
                                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                                null
                        )
                )
        ).thenReturn(Optional.empty());

        String payload =
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                );

        assertThrows(
                IllegalStateException.class,
                () -> process(payload)
        );

        verifyRejectedPayment();
    }

    @Test
    void shouldRejectPaymentWithDifferentExternalReference() {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        String payload =
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                ).replace(
                        PAYMENT_INTENT_ID,
                        "pi_different_payment"
                );

        assertThrows(
                IllegalStateException.class,
                () -> process(payload)
        );

        verifyRejectedPayment();
    }

    @Test
    void shouldRejectRenewalPaymentForPreviousBillingPeriod() {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_RENEWAL,
                PERIOD_END
        );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(
                        Instant.parse(
                                "2027-01-21T10:00:00Z"
                        )
                );

        String payload =
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_RENEWAL,
                        PERIOD_END,
                        null
                );

        assertThrows(
                IllegalStateException.class,
                () -> process(payload)
        );

        verifyRejectedPayment();
    }

    @Test
    void shouldIgnorePaymentFailureReceivedAfterSuccess()
            throws Exception {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        when(
                processedEventRepository.existsById(
                        eventKey(PAYMENT_SUCCEEDED)
                )
        ).thenReturn(true);

        process(
                createPayload(
                        PAYMENT_FAILED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        "Previous payment failure"
                )
        );

        verifyNoPaymentHandlers();

        verify(processedEventRepository)
                .save(any());
    }

    @Test
    void shouldNotMarkEventAsProcessedWhenConfirmationFails() {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        when(
                paymentConfirmedHandler.handle(
                        any(ConfirmSubscriptionPaymentCommand.class)
                )
        ).thenThrow(
                new IllegalStateException(
                        "Unable to synchronize entitlements"
                )
        );

        String payload =
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                );

        assertThrows(
                IllegalStateException.class,
                () -> process(payload)
        );

        verify(paymentConfirmedHandler)
                .handle(
                        any(ConfirmSubscriptionPaymentCommand.class)
                );

        verify(processedEventRepository, never())
                .save(any());
    }

    @Test
    void shouldAllowSuccessfulPaymentAfterPreviousFailure()
            throws Exception {

        prepareOperation(
                PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                null
        );

        process(
                createPayload(
                        PAYMENT_FAILED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        "Your card was declined."
                )
        );

        when(
                processedEventRepository.existsById(
                        eventKey(PAYMENT_FAILED)
                )
        ).thenReturn(true);

        process(
                createPayload(
                        PAYMENT_SUCCEEDED,
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        null,
                        null
                )
        );

        verify(paymentFailedHandler)
                .handle(
                        any(FailSubscriptionPaymentCommand.class)
                );

        verify(paymentConfirmedHandler)
                .handle(
                        any(ConfirmSubscriptionPaymentCommand.class)
                );

        verify(processedEventRepository, times(2))
                .save(any());

        verifyNoInteractions(
                renewalPaymentConfirmedHandler,
                renewalPaymentFailedHandler
        );
    }

    private void prepareOperation(
            PaymentPurpose purpose,
            Instant periodEnd
    ) {

        Instant now =
                Instant.parse(
                        "2026-11-25T10:00:00Z"
                );

        String key =
                operationKey(
                        purpose,
                        periodEnd
                );

        PaymentOperationPersistenceEntity operation =
                new PaymentOperationPersistenceEntity(
                        key,
                        subscriptionId,
                        subscriberId,
                        planId,
                        "stripe",
                        purpose,
                        new BigDecimal("29.90"),
                        "PEN",
                        periodEnd,
                        now
                );

        operation.registerExternalReference(
                PAYMENT_INTENT_ID,
                now
        );

        when(
                paymentOperationRepository.findByIdForUpdate(key)
        ).thenReturn(
                Optional.of(operation)
        );

        when(subscription.getId())
                .thenReturn(
                        new SubscriptionId(subscriptionId)
                );

        when(subscription.getSubscriberId())
                .thenReturn(
                        new SubscriberId(subscriberId)
                );

        when(subscription.getPlanId())
                .thenReturn(
                        new PlanId(planId)
                );

        when(subscription.getStatus())
                .thenReturn(
                        purpose == PaymentPurpose.SUBSCRIPTION_ACTIVATION
                                ? SubscriptionStatus.PENDING
                                : SubscriptionStatus.ACTIVE
                );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(periodEnd);

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(false);

        when(
                subscriptionRepository.findById(
                        new SubscriptionId(subscriptionId)
                )
        ).thenReturn(
                Optional.of(subscription)
        );
    }

    private String operationKey(
            PaymentPurpose purpose,
            Instant periodEnd
    ) {

        return new PaymentRequest(
                subscriptionId,
                subscriberId,
                planId,
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                purpose,
                periodEnd
        ).idempotencyKey();
    }

    private String eventKey(
            String eventType
    ) {

        return eventType
                + ":"
                + PAYMENT_INTENT_ID;
    }

    private void process(
            String payload
    ) throws Exception {

        String signature =
                Webhook.Signature.generateSignatureHeader(
                        payload,
                        WEBHOOK_SECRET
                );

        service.process(
                payload,
                signature
        );
    }

    private void verifyNoPaymentHandlers() {

        verifyNoInteractions(
                paymentConfirmedHandler,
                paymentFailedHandler,
                renewalPaymentConfirmedHandler,
                renewalPaymentFailedHandler
        );
    }

    private void verifyRejectedPayment() {

        verifyNoPaymentHandlers();

        verify(processedEventRepository, never())
                .save(any());
    }

    private String createPayload(
            String eventType,
            PaymentPurpose purpose,
            Instant periodEnd,
            String failureMessage
    ) {

        boolean successful =
                PAYMENT_SUCCEEDED.equals(eventType);

        String status =
                successful
                        ? "succeeded"
                        : "requires_payment_method";

        long amountReceived =
                successful
                        ? 2990
                        : 0;

        String lastPaymentError =
                failureMessage == null
                        ? "null"
                        : """
                        {
                          "message": "%s"
                        }
                        """.formatted(failureMessage);

        String periodMetadata =
                periodEnd == null
                        ? ""
                        : """
                          ,
                          "currentPeriodEnd": "%s"
                          """.formatted(periodEnd);

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
                      "amount": 2990,
                      "amount_received": %d,
                      "currency": "pen",
                      "status": "%s",
                      "metadata": {
                        "subscriptionId": "%s",
                        "subscriberId": "%s",
                        "planId": "%s",
                        "purpose": "%s",
                        "operationKey": "%s"%s
                      },
                      "last_payment_error": %s
                    }
                  }
                }
                """.formatted(
                        "evt_" + UUID.randomUUID(),
                        Stripe.API_VERSION,
                        eventType,
                        PAYMENT_INTENT_ID,
                        amountReceived,
                        status,
                        subscriptionId,
                        subscriberId,
                        planId,
                        purpose.name(),
                        operationKey(
                                purpose,
                                periodEnd
                        ),
                        periodMetadata,
                        lastPaymentError
                );
    }
}