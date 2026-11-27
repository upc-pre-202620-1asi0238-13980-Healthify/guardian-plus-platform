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
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.ProcessedPaymentWebhookEventPersistenceEntity;
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

import java.time.Instant;
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

    private static final String METADATA_SUBSCRIPTION_ID =
            "subscriptionId";

    private static final String METADATA_PURPOSE =
            "purpose";

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

    public StripeWebhookService(
            @Value("${subscriptions.billing.stripe.webhook-secret}")
            String webhookSecret,
            PaymentConfirmedHandler paymentConfirmedHandler,
            PaymentFailedHandler paymentFailedHandler,
            RenewalPaymentConfirmedHandler renewalPaymentConfirmedHandler,
            RenewalPaymentFailedHandler renewalPaymentFailedHandler,
            ProcessedPaymentWebhookEventPersistenceRepository
                    processedEventRepository
    ) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException(
                    "Stripe webhook secret must be configured when Stripe payment provider is enabled"
            );
        }

        this.webhookSecret = webhookSecret;

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

        String eventKey =
                createEventKey(
                        event.getType(),
                        paymentIntent.getId()
                );

        if (processedEventRepository.existsById(eventKey)) {
            return;
        }

        PaymentMetadata metadata =
                extractMetadata(paymentIntent);

        switch (event.getType()) {

            case PAYMENT_SUCCEEDED_EVENT ->
                    handleSuccessfulPayment(
                            metadata,
                            paymentIntent.getId()
                    );

            case PAYMENT_FAILED_EVENT ->
                    handleFailedPayment(
                            metadata,
                            paymentIntent
                    );

            default -> {
                return;
            }
        }

        processedEventRepository.save(
                new ProcessedPaymentWebhookEventPersistenceEntity(
                        eventKey,
                        event.getId(),
                        paymentIntent.getId(),
                        event.getType(),
                        Instant.now()
                )
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

    private PaymentMetadata extractMetadata(
            PaymentIntent paymentIntent
    ) {

        Map<String, String> metadata =
                paymentIntent.getMetadata();

        if (metadata == null) {
            throw new IllegalStateException(
                    "Stripe PaymentIntent metadata is missing"
            );
        }

        String subscriptionIdValue =
                metadata.get(
                        METADATA_SUBSCRIPTION_ID
                );

        String purposeValue =
                metadata.get(
                        METADATA_PURPOSE
                );

        if (subscriptionIdValue == null
                || subscriptionIdValue.isBlank()) {

            throw new IllegalStateException(
                    "Stripe PaymentIntent subscriptionId metadata is missing"
            );
        }

        if (purposeValue == null
                || purposeValue.isBlank()) {

            throw new IllegalStateException(
                    "Stripe PaymentIntent purpose metadata is missing"
            );
        }

        UUID subscriptionId;

        try {
            subscriptionId =
                    UUID.fromString(
                            subscriptionIdValue
                    );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Stripe PaymentIntent subscriptionId metadata is invalid",
                    exception
            );
        }

        PaymentPurpose purpose;

        try {
            purpose =
                    PaymentPurpose.valueOf(
                            purposeValue
                    );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Stripe PaymentIntent purpose metadata is invalid",
                    exception
            );
        }

        return new PaymentMetadata(
                subscriptionId,
                purpose
        );
    }

    private void handleSuccessfulPayment(
            PaymentMetadata metadata,
            String externalReference
    ) {

        switch (metadata.purpose()) {

            case SUBSCRIPTION_ACTIVATION ->
                    paymentConfirmedHandler.handle(
                            new ConfirmSubscriptionPaymentCommand(
                                    metadata.subscriptionId(),
                                    externalReference
                            )
                    );

            case SUBSCRIPTION_RENEWAL ->
                    renewalPaymentConfirmedHandler.handle(
                            new ConfirmRenewalPaymentCommand(
                                    metadata.subscriptionId(),
                                    externalReference
                            )
                    );
        }
    }

    private void handleFailedPayment(
            PaymentMetadata metadata,
            PaymentIntent paymentIntent
    ) {

        String failureReason =
                extractFailureReason(
                        paymentIntent
                );

        switch (metadata.purpose()) {

            case SUBSCRIPTION_ACTIVATION ->
                    paymentFailedHandler.handle(
                            new FailSubscriptionPaymentCommand(
                                    metadata.subscriptionId(),
                                    paymentIntent.getId(),
                                    failureReason
                            )
                    );

            case SUBSCRIPTION_RENEWAL ->
                    renewalPaymentFailedHandler.handle(
                            new FailRenewalPaymentCommand(
                                    metadata.subscriptionId(),
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

    private String createEventKey(
            String eventType,
            String externalReference
    ) {

        return eventType
                + ":"
                + externalReference;
    }

    private record PaymentMetadata(
            UUID subscriptionId,
            PaymentPurpose purpose
    ) {
    }
}