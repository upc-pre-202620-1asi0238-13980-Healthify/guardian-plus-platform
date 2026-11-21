package com.healthify.guardian.platform.subscriptions.infrastructure.payments.stripe;

import com.stripe.exception.SignatureVerificationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@ConditionalOnProperty(
        name = "subscriptions.billing.payment-provider",
        havingValue = "stripe"
)
public class StripeWebhookController {

    private static final String STRIPE_SIGNATURE_HEADER =
            "Stripe-Signature";

    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(
            StripeWebhookService stripeWebhookService
    ) {
        this.stripeWebhookService =
                Objects.requireNonNull(
                        stripeWebhookService
                );
    }

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody String payload,
            @RequestHeader(STRIPE_SIGNATURE_HEADER)
            String signatureHeader
    ) {

        try {

            stripeWebhookService.process(
                    payload,
                    signatureHeader
            );

            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException exception) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }
}