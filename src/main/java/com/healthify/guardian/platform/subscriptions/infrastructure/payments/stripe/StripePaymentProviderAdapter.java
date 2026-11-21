package com.healthify.guardian.platform.subscriptions.infrastructure.payments.stripe;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentProvider;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Locale;
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

    private final StripeClient stripeClient;

    public StripePaymentProviderAdapter(
            @Value("${subscriptions.billing.stripe.secret-key}")
            String secretKey
    ) {

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "Stripe secret key must be configured when Stripe payment provider is enabled"
            );
        }

        this.stripeClient = new StripeClient(secretKey);
    }

    @Override
    public PaymentInitiationResult initiatePayment(
            PaymentRequest request
    ) {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );

        try {

            long amountInMinorUnits =
                    convertToMinorUnits(request);

            PaymentIntentCreateParams params =
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
                            .build();

            PaymentIntent paymentIntent =
                    stripeClient
                            .v1()
                            .paymentIntents()
                            .create(params);

            if (paymentIntent.getClientSecret() == null
                    || paymentIntent.getClientSecret().isBlank()) {

                throw new IllegalStateException(
                        "Stripe PaymentIntent did not return a client secret"
                );
            }

            return new PaymentInitiationResult(
                    PROVIDER_NAME,
                    paymentIntent.getId(),
                    PaymentStatus.PENDING,
                    paymentIntent.getClientSecret()
            );

        } catch (StripeException exception) {

            LOGGER.error(
                    "Stripe payment initiation failed. statusCode={}, requestId={}, message={}",
                    exception.getStatusCode(),
                    exception.getRequestId(),
                    exception.getMessage()
            );

            throw new IllegalStateException(
                    "Unable to initiate Stripe payment",
                    exception
            );
        }
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