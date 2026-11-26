package com.healthify.guardian.platform.subscriptions.infrastructure.payments.stripe;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PaymentOperationPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.ProcessedPaymentWebhookEventPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.PaymentOperationPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.ProcessedPaymentWebhookEventPersistenceRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@ConditionalOnProperty(
        name = "subscriptions.billing.payment-provider",
        havingValue = "stripe"
)
public class StripeWebhookService {

    private static final String PAYMENT_SUCCEEDED_EVENT =
            "payment_intent.succeeded";

    private static final String PAYMENT_FAILED_EVENT =
            "payment_intent.payment_failed";

    private static final String PROVIDER_NAME =
            "stripe";

    private static final String DEFAULT_FAILURE_REASON =
            "Stripe payment failed";

    private final String webhookSecret;

    private final PaymentConfirmedHandler
            paymentConfirmedHandler;

    private final PaymentFailedHandler
            paymentFailedHandler;

    private final RenewalPaymentConfirmedHandler
            renewalPaymentConfirmedHandler;

    private final RenewalPaymentFailedHandler
            renewalPaymentFailedHandler;

    private final ProcessedPaymentWebhookEventPersistenceRepository
            processedEventRepository;

    private final PaymentOperationPersistenceRepository
            paymentOperationRepository;

    private final SubscriptionRepository
            subscriptionRepository;

    public StripeWebhookService(
            @Value("${subscriptions.billing.stripe.webhook-secret}")
            String webhookSecret,
            PaymentConfirmedHandler paymentConfirmedHandler,
            PaymentFailedHandler paymentFailedHandler,
            RenewalPaymentConfirmedHandler renewalPaymentConfirmedHandler,
            RenewalPaymentFailedHandler renewalPaymentFailedHandler,
            ProcessedPaymentWebhookEventPersistenceRepository
                    processedEventRepository,
            PaymentOperationPersistenceRepository
                    paymentOperationRepository,
            SubscriptionRepository subscriptionRepository
    ) {

        if (webhookSecret == null || webhookSecret.isBlank()) {

            throw new IllegalStateException(
                    "Stripe webhook secret must be configured when Stripe payment provider is enabled"
            );
        }

        this.webhookSecret =
                webhookSecret;

        this.paymentConfirmedHandler =
                Objects.requireNonNull(
                        paymentConfirmedHandler
                );

        this.paymentFailedHandler =
                Objects.requireNonNull(
                        paymentFailedHandler
                );

        this.renewalPaymentConfirmedHandler =
                Objects.requireNonNull(
                        renewalPaymentConfirmedHandler
                );

        this.renewalPaymentFailedHandler =
                Objects.requireNonNull(
                        renewalPaymentFailedHandler
                );

        this.processedEventRepository =
                Objects.requireNonNull(
                        processedEventRepository
                );

        this.paymentOperationRepository =
                Objects.requireNonNull(
                        paymentOperationRepository
                );

        this.subscriptionRepository =
                Objects.requireNonNull(
                        subscriptionRepository
                );
    }

    @Transactional
    public void process(
            String payload,
            String signatureHeader
    ) throws SignatureVerificationException {

        Objects.requireNonNull(
                payload,
                "payload must not be null"
        );

        Objects.requireNonNull(
                signatureHeader,
                "signatureHeader must not be null"
        );

        Event event =
                Webhook.constructEvent(
                        payload,
                        signatureHeader,
                        webhookSecret
                );

        if (!isSupportedEvent(event.getType())) {
            return;
        }

        PaymentIntent paymentIntent =
                extractPaymentIntent(event);

        String externalReference =
                paymentIntent.getId();

        if (externalReference == null
                || externalReference.isBlank()) {

            throw new IllegalStateException(
                    "Stripe PaymentIntent reference is missing"
            );
        }

        String eventKey =
                createEventKey(
                        event.getType(),
                        externalReference
                );

        if (processedEventRepository.existsById(eventKey)) {
            return;
        }

        Map<String, String> metadata =
                paymentIntent.getMetadata();

        if (metadata == null) {

            throw new IllegalStateException(
                    "Stripe PaymentIntent metadata is missing"
            );
        }

        String operationKey =
                requireMetadata(
                        metadata,
                        "operationKey"
                );

        UUID subscriberId =
                parseUuid(
                        requireMetadata(
                                metadata,
                                "subscriberId"
                        ),
                        "subscriberId"
                );

        subscriptionRepository.acquireSubscriberLock(
                new SubscriberId(subscriberId)
        );

        PaymentOperationPersistenceEntity operation =
                paymentOperationRepository
                        .findByIdForUpdate(operationKey)
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Stripe payment operation not found"
                                )
                        );

        /*
         * Verificar nuevamente despues de adquirir los bloqueos.
         * Otra transaccion podria haber procesado el evento.
         */
        if (processedEventRepository.existsById(eventKey)) {
            return;
        }

        validatePaymentIntent(
                paymentIntent,
                operation,
                metadata,
                event.getType()
        );

        boolean successfulPayment =
                PAYMENT_SUCCEEDED_EVENT.equals(
                        event.getType()
                );

        /*
         * Un fallo recibido despues de una confirmacion
         * no debe modificar la suscripcion.
         */
        if (!successfulPayment
                && processedEventRepository.existsById(
                        createEventKey(
                                PAYMENT_SUCCEEDED_EVENT,
                                externalReference
                        )
                )) {

            saveProcessedEvent(
                    event,
                    eventKey,
                    externalReference
            );

            return;
        }

        Subscription subscription =
                subscriptionRepository
                        .findById(
                                new SubscriptionId(
                                        operation.getSubscriptionId()
                                )
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Subscription associated with payment was not found"
                                )
                        );

        if (!subscription.getSubscriberId()
                .value()
                .equals(operation.getSubscriberId())) {

            throw new IllegalStateException(
                    "Payment operation subscriber does not match subscription"
            );
        }

        boolean currentOperation =
                isCurrentOperation(
                        subscription,
                        operation
                );

        if (!currentOperation) {

            if (successfulPayment) {

                throw new IllegalStateException(
                        "Confirmed payment does not match the current subscription state or billing period. Reconciliation required."
                );
            }

            /*
             * Un fallo antiguo no cambia el periodo
             * ni el estado actual de la suscripcion.
             */
            saveProcessedEvent(
                    event,
                    eventKey,
                    externalReference
            );

            return;
        }

        if (successfulPayment) {

            handleSuccessfulPayment(
                    operation,
                    externalReference
            );

        } else {

            handleFailedPayment(
                    operation,
                    paymentIntent
            );
        }

        saveProcessedEvent(
                event,
                eventKey,
                externalReference
        );
    }

    private boolean isSupportedEvent(
            String eventType
    ) {

        return PAYMENT_SUCCEEDED_EVENT.equals(eventType)
                || PAYMENT_FAILED_EVENT.equals(eventType);
    }

    private PaymentIntent extractPaymentIntent(
            Event event
    ) {

        StripeObject stripeObject =
                event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Unable to deserialize Stripe webhook event data"
                                )
                        );

        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {

            throw new IllegalStateException(
                    "Stripe webhook event does not contain a PaymentIntent"
            );
        }

        return paymentIntent;
    }

    private void validatePaymentIntent(
            PaymentIntent paymentIntent,
            PaymentOperationPersistenceEntity operation,
            Map<String, String> metadata,
            String eventType
    ) {

        if (!PROVIDER_NAME.equals(operation.getProvider())) {

            throw new IllegalStateException(
                    "Payment operation provider is invalid"
            );
        }

        if (operation.getExternalReference() == null
                || !operation.getExternalReference()
                        .equals(paymentIntent.getId())) {

            throw new IllegalStateException(
                    "Stripe PaymentIntent does not match the stored payment reference"
            );
        }

        long expectedAmount =
                calculateExpectedAmount(operation);

        String expectedCurrency =
                operation.getCurrency()
                        .toLowerCase(Locale.ROOT);

        if (!Objects.equals(
                paymentIntent.getAmount(),
                expectedAmount
        ) || !Objects.equals(
                paymentIntent.getCurrency(),
                expectedCurrency
        )) {

            throw new IllegalStateException(
                    "Stripe payment amount or currency does not match the stored operation"
            );
        }

        boolean metadataMatches =
                Objects.equals(
                        metadata.get("operationKey"),
                        operation.getIdempotencyKey()
                )

                && Objects.equals(
                        metadata.get("subscriptionId"),
                        operation.getSubscriptionId().toString()
                )

                && Objects.equals(
                        metadata.get("subscriberId"),
                        operation.getSubscriberId().toString()
                )

                && Objects.equals(
                        metadata.get("planId"),
                        operation.getPlanId().toString()
                )

                && Objects.equals(
                        metadata.get("purpose"),
                        operation.getPurpose().name()
                );

        String expectedPeriodEnd =
                operation.getCurrentPeriodEnd() == null
                        ? null
                        : operation.getCurrentPeriodEnd().toString();

        metadataMatches =
                metadataMatches
                        && Objects.equals(
                                metadata.get("currentPeriodEnd"),
                                expectedPeriodEnd
                        );

        if (!metadataMatches) {

            throw new IllegalStateException(
                    "Stripe payment metadata does not match the stored operation"
            );
        }

        if (PAYMENT_SUCCEEDED_EVENT.equals(eventType)) {

            if (!"succeeded".equals(paymentIntent.getStatus())) {

                throw new IllegalStateException(
                        "Stripe PaymentIntent is not in succeeded status"
                );
            }

            if (!Objects.equals(
                    paymentIntent.getAmountReceived(),
                    expectedAmount
            )) {

                throw new IllegalStateException(
                        "Stripe received amount does not match the expected amount"
                );
            }
        }
    }

    private long calculateExpectedAmount(
            PaymentOperationPersistenceEntity operation
    ) {

        Currency currency =
                Currency.getInstance(
                        operation.getCurrency()
                );

        int fractionDigits =
                currency.getDefaultFractionDigits();

        if (fractionDigits < 0) {

            throw new IllegalStateException(
                    "Payment operation currency is unsupported"
            );
        }

        long amount =
                operation.getAmount()
                        .setScale(
                                fractionDigits,
                                RoundingMode.UNNECESSARY
                        )
                        .movePointRight(fractionDigits)
                        .longValueExact();

        if (amount <= 0) {

            throw new IllegalStateException(
                    "Payment operation amount must be greater than zero"
            );
        }

        return amount;
    }

    private boolean isCurrentOperation(
            Subscription subscription,
            PaymentOperationPersistenceEntity operation
    ) {

        if (!subscription.getPlanId()
                .value()
                .equals(operation.getPlanId())) {

            return false;
        }

        return switch (operation.getPurpose()) {

            case SUBSCRIPTION_ACTIVATION ->

                    subscription.getStatus()
                            == SubscriptionStatus.PENDING

                    && operation.getCurrentPeriodEnd() == null;

            case SUBSCRIPTION_RENEWAL ->

                    subscription.getStatus()
                            == SubscriptionStatus.ACTIVE

                    && !subscription.isCancelAtPeriodEnd()

                    && operation.getCurrentPeriodEnd() != null

                    && Objects.equals(
                            subscription.getCurrentPeriodEnd(),
                            operation.getCurrentPeriodEnd()
                    );
        };
    }

    private void handleSuccessfulPayment(
            PaymentOperationPersistenceEntity operation,
            String externalReference
    ) {

        switch (operation.getPurpose()) {

            case SUBSCRIPTION_ACTIVATION ->

                    paymentConfirmedHandler.handle(
                            new ConfirmSubscriptionPaymentCommand(
                                    operation.getSubscriptionId(),
                                    externalReference
                            )
                    );

            case SUBSCRIPTION_RENEWAL ->

                    renewalPaymentConfirmedHandler.handle(
                            new ConfirmRenewalPaymentCommand(
                                    operation.getSubscriptionId(),
                                    externalReference
                            )
                    );
        }
    }

    private void handleFailedPayment(
            PaymentOperationPersistenceEntity operation,
            PaymentIntent paymentIntent
    ) {

        String failureReason =
                extractFailureReason(paymentIntent);

        switch (operation.getPurpose()) {

            case SUBSCRIPTION_ACTIVATION ->

                    paymentFailedHandler.handle(
                            new FailSubscriptionPaymentCommand(
                                    operation.getSubscriptionId(),
                                    paymentIntent.getId(),
                                    failureReason
                            )
                    );

            case SUBSCRIPTION_RENEWAL ->

                    renewalPaymentFailedHandler.handle(
                            new FailRenewalPaymentCommand(
                                    operation.getSubscriptionId(),
                                    paymentIntent.getId(),
                                    failureReason
                            )
                    );
        }
    }

    private String extractFailureReason(
            PaymentIntent paymentIntent
    ) {

        if (paymentIntent.getLastPaymentError() == null) {
            return DEFAULT_FAILURE_REASON;
        }

        String message =
                paymentIntent
                        .getLastPaymentError()
                        .getMessage();

        if (message == null || message.isBlank()) {
            return DEFAULT_FAILURE_REASON;
        }

        return message;
    }

    private String requireMetadata(
            Map<String, String> metadata,
            String field
    ) {

        String value =
                metadata.get(field);

        if (value == null || value.isBlank()) {

            throw new IllegalStateException(
                    "Stripe PaymentIntent "
                            + field
                            + " metadata is missing"
            );
        }

        return value;
    }

    private UUID parseUuid(
            String value,
            String field
    ) {

        try {

            return UUID.fromString(value);

        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "Stripe PaymentIntent "
                            + field
                            + " metadata is invalid",
                    exception
            );
        }
    }

    private String createEventKey(
            String eventType,
            String externalReference
    ) {

        return eventType
                + ":"
                + externalReference;
    }

    private void saveProcessedEvent(
            Event event,
            String eventKey,
            String externalReference
    ) {

        processedEventRepository.save(
                new ProcessedPaymentWebhookEventPersistenceEntity(
                        eventKey,
                        event.getId(),
                        externalReference,
                        event.getType(),
                        Instant.now()
                )
        );
    }
}