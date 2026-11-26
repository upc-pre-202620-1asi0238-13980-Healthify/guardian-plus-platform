
package com.healthify.guardian.platform.subscriptions.interfaces.rest.configuration;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SubscriptionsOpenApiConfiguration {

    @Bean
    public OpenApiCustomizer subscriptionsOpenApiCustomizer() {

        return openApi -> {

            if (openApi.getPaths() == null) {
                return;
            }

            updatePostOperation(
                    openApi.getPaths().get(
                            "/api/v1/subscriptions/{subscriptionId}/payment"
                    ),
                    """
                    Initiates the payment required to activate a pending
                    subscription.

                    Payment initiation is idempotent for the subscription.
                    Repeated requests reuse the existing Stripe PaymentIntent.

                    When Stripe is enabled, the subscription is activated
                    only after successful payment confirmation through
                    a verified webhook.

                    The clientSecret, when provided, can be used by the
                    mobile application to complete the payment.
                    """
            );

            updatePostOperation(
                    openApi.getPaths().get(
                            "/api/v1/subscriptions/{subscriptionId}/renewal/payment"
                    ),
                    """
                    Initiates a renewal payment for an active subscription.

                    Renewal payment initiation is idempotent per subscription
                    and current billing period.

                    Repeated requests for the same billing period reuse
                    the existing Stripe PaymentIntent.

                    A successful Stripe webhook extends the subscription
                    according to its monthly or annual billing cycle.

                    Payment initiation alone does not extend the subscription.

                    Automatic recurring Stripe charges are not currently
                    supported.
                    """
            );

            PathItem webhookPath =
                    openApi.getPaths().get(
                            "/api/v1/webhooks/stripe"
                    );

            if (webhookPath != null
                    && webhookPath.getPost() != null) {

                Operation webhook =
                        webhookPath.getPost();

                webhook.setSummary(
                        "Process Stripe payment webhook"
                );

                webhook.setDescription(
                        """
                        Processes signed Stripe payment events.

                        Supported events:
                        - payment_intent.succeeded
                        - payment_intent.payment_failed

                        Validates the Stripe-Signature header, payment
                        reference, amount, currency, metadata and billing
                        period against the persisted payment operation.

                        Duplicate events do not activate or renew
                        subscriptions more than once.

                        This endpoint is intended for Stripe webhook
                        delivery and requires a valid Stripe signature.
                        """
                );

                webhook.setSecurity(List.of());
            }
        };
    }

    private void updatePostOperation(
            PathItem pathItem,
            String description
    ) {

        if (pathItem != null
                && pathItem.getPost() != null) {

            pathItem.getPost()
                    .setDescription(description);
        }
    }
}
