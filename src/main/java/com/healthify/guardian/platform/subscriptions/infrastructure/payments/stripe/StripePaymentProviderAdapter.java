package com.healthify.guardian.platform.subscriptions.infrastructure.payments.stripe;

import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentProvider;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters.PaymentOperationStore;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters.PaymentOperationStore.PaymentOperationSnapshot;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnProperty(
        name = "subscriptions.billing.payment-provider",
        havingValue = "stripe"
)
public class StripePaymentProviderAdapter implements PaymentProvider {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    StripePaymentProviderAdapter.class
            );

    private static final String PROVIDER_NAME = "stripe";

    private static final Duration SAFE_RETRY_WINDOW =
            Duration.ofHours(23);

    private final StripeClient stripeClient;
    private final PaymentOperationStore paymentOperationStore;

    public StripePaymentProviderAdapter(
            @Value("${subscriptions.billing.stripe.secret-key}")
            String secretKey,
            PaymentOperationStore paymentOperationStore
    ) {

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "Stripe secret key must be configured when Stripe payment provider is enabled"
            );
        }

        this.stripeClient =
                new StripeClient(secretKey);

        this.paymentOperationStore =
                Objects.requireNonNull(paymentOperationStore);
    }

    @Override
    public PaymentInitiationResult initiatePayment(
            PaymentRequest request
    ) {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );

        long amountInMinorUnits =
                convertToMinorUnits(request);

        if (amountInMinorUnits <= 0) {

            throw new IllegalArgumentException(
                    "Stripe payment amount must be greater than zero"
            );
        }

        PaymentOperationSnapshot operation =
                paymentOperationStore.reserve(
                        request,
                        PROVIDER_NAME
                );

        try {

            String externalReference;

            if (operation.hasExternalReference()) {

                externalReference =
                        operation.externalReference();

            } else {

                validateRetryWindow(operation);

                PaymentIntent created =
                        createPaymentIntent(
                                request,
                                amountInMinorUnits
                        );

                validatePaymentIntent(
                        created,
                        request,
                        amountInMinorUnits
                );

                PaymentOperationSnapshot registered =
                        paymentOperationStore
                                .registerExternalReference(
                                        request,
                                        PROVIDER_NAME,
                                        created.getId()
                                );

                externalReference =
                        registered.externalReference();
            }

            PaymentIntent paymentIntent =
                    retrievePaymentIntent(
                            externalReference
                    );

            validatePaymentIntent(
                    paymentIntent,
                    request,
                    amountInMinorUnits
            );

            if (!externalReference.equals(paymentIntent.getId())) {

                throw new SubscriptionPaymentException(
                        "Stripe payment reference does not match the stored operation"
                );
            }

            return toPaymentInitiationResult(
                    paymentIntent
            );

        } catch (StripeException exception) {

            LOGGER.error(
                    "Stripe payment initiation failed. statusCode={}, requestId={}",
                    exception.getStatusCode(),
                    exception.getRequestId()
            );

            throw new IllegalStateException(
                    "Unable to initiate Stripe payment",
                    exception
            );
        }
    }

    private PaymentIntent createPaymentIntent(
            PaymentRequest request,
            long amountInMinorUnits
    ) throws StripeException {

        PaymentIntentCreateParams.Builder builder =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInMinorUnits)
                        .setCurrency(
                                request.currency()
                                        .getCurrencyCode()
                                        .toLowerCase(Locale.ROOT)
                        )
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams
                                        .AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .setAllowRedirects(
                                                PaymentIntentCreateParams
                                                        .AutomaticPaymentMethods
                                                        .AllowRedirects
                                                        .NEVER
                                        )
                                        .build()
                        )
                        .putMetadata(
                                "subscriptionId",
                                request.subscriptionId().toString()
                        )
                        .putMetadata(
                                "subscriberId",
                                request.subscriberId().toString()
                        )
                        .putMetadata(
                                "planId",
                                request.planId().toString()
                        )
                        .putMetadata(
                                "purpose",
                                request.purpose().name()
                        )
                        .putMetadata(
                                "operationKey",
                                request.idempotencyKey()
                        );

        if (request.currentPeriodEnd() != null) {

            builder.putMetadata(
                    "currentPeriodEnd",
                    request.currentPeriodEnd().toString()
            );
        }

        PaymentIntentCreateParams params =
                builder.build();

        RequestOptions options =
                RequestOptions.builder()
                        .setIdempotencyKey(
                                request.idempotencyKey()
                        )
                        .setMaxNetworkRetries(2)
                        .build();

        return stripeClient
                .v1()
                .paymentIntents()
                .create(
                        params,
                        options
                );
    }

    private PaymentIntent retrievePaymentIntent(
            String externalReference
    ) throws StripeException {

        PaymentIntentRetrieveParams params =
                PaymentIntentRetrieveParams
                        .builder()
                        .build();

        return stripeClient
                .v1()
                .paymentIntents()
                .retrieve(
                        externalReference,
                        params
                );
    }

    private void validateRetryWindow(
            PaymentOperationSnapshot operation
    ) {

        Instant retryDeadline =
                operation.createdAt()
                        .plus(SAFE_RETRY_WINDOW);

        if (!Instant.now().isBefore(retryDeadline)) {

            throw new SubscriptionPaymentException(
                    "Payment operation requires reconciliation before retrying"
            );
        }
    }

    private void validatePaymentIntent(
            PaymentIntent paymentIntent,
            PaymentRequest request,
            long expectedAmount
    ) {

        if (paymentIntent == null
                || paymentIntent.getId() == null
                || paymentIntent.getId().isBlank()) {

            throw new SubscriptionPaymentException(
                    "Stripe returned an invalid PaymentIntent"
            );
        }

        String expectedCurrency =
                request.currency()
                        .getCurrencyCode()
                        .toLowerCase(Locale.ROOT);

        if (!Objects.equals(
                paymentIntent.getAmount(),
                expectedAmount
        ) || !Objects.equals(
                paymentIntent.getCurrency(),
                expectedCurrency
        )) {

            throw new SubscriptionPaymentException(
                    "Stripe payment amount or currency does not match the original request"
            );
        }

        Map<String, String> metadata =
                paymentIntent.getMetadata();

        if (metadata == null) {

            throw new SubscriptionPaymentException(
                    "Stripe PaymentIntent metadata is missing"
            );
        }

        boolean metadataMatches =
                Objects.equals(
                        metadata.get("subscriptionId"),
                        request.subscriptionId().toString()
                )

                && Objects.equals(
                        metadata.get("subscriberId"),
                        request.subscriberId().toString()
                )

                && Objects.equals(
                        metadata.get("planId"),
                        request.planId().toString()
                )

                && Objects.equals(
                        metadata.get("purpose"),
                        request.purpose().name()
                )

                && Objects.equals(
                        metadata.get("operationKey"),
                        request.idempotencyKey()
                );

        String expectedPeriodEnd =
                request.currentPeriodEnd() == null
                        ? null
                        : request.currentPeriodEnd().toString();

        metadataMatches =
                metadataMatches
                        && Objects.equals(
                                metadata.get("currentPeriodEnd"),
                                expectedPeriodEnd
                        );

        if (!metadataMatches) {

            throw new SubscriptionPaymentException(
                    "Stripe payment metadata does not match the original operation"
            );
        }

        if ("succeeded".equals(paymentIntent.getStatus())
                && !Objects.equals(
                        paymentIntent.getAmountReceived(),
                        expectedAmount
                )) {

            throw new SubscriptionPaymentException(
                    "Stripe received amount does not match the expected payment amount"
            );
        }
    }

    private PaymentInitiationResult toPaymentInitiationResult(
            PaymentIntent paymentIntent
    ) {

        String status =
                paymentIntent.getStatus();

        if (status == null) {

            throw new SubscriptionPaymentException(
                    "Stripe PaymentIntent status is missing"
            );
        }

        return switch (status) {

            case "requires_payment_method",
                 "requires_confirmation",
                 "requires_action" ->

                    new PaymentInitiationResult(
                            PROVIDER_NAME,
                            paymentIntent.getId(),
                            PaymentStatus.PENDING,
                            requireClientSecret(paymentIntent)
                    );

            case "processing",
                 "requires_capture" ->

                    new PaymentInitiationResult(
                            PROVIDER_NAME,
                            paymentIntent.getId(),
                            PaymentStatus.PENDING
                    );

            case "succeeded" ->

                    new PaymentInitiationResult(
                            PROVIDER_NAME,
                            paymentIntent.getId(),
                            PaymentStatus.CONFIRMED
                    );

            case "canceled" ->

                    new PaymentInitiationResult(
                            PROVIDER_NAME,
                            paymentIntent.getId(),
                            PaymentStatus.FAILED
                    );

            default ->

                    throw new SubscriptionPaymentException(
                            "Unsupported Stripe PaymentIntent status"
                    );
        };
    }

    private String requireClientSecret(
            PaymentIntent paymentIntent
    ) {

        String clientSecret =
                paymentIntent.getClientSecret();

        if (clientSecret == null
                || clientSecret.isBlank()) {

            throw new SubscriptionPaymentException(
                    "Stripe PaymentIntent did not return a client secret"
            );
        }

        return clientSecret;
    }

    private long convertToMinorUnits(
            PaymentRequest request
    ) {

        int fractionDigits =
                request.currency()
                        .getDefaultFractionDigits();

        if (fractionDigits < 0) {

            throw new IllegalArgumentException(
                    "Unsupported currency: "
                            + request.currency().getCurrencyCode()
            );
        }

        return request.amount()
                .setScale(
                        fractionDigits,
                        RoundingMode.UNNECESSARY
                )
                .movePointRight(fractionDigits)
                .longValueExact();
    }
}