# Guardian+ Subscriptions and Billing

## Overview

Guardian+ provides subscription lifecycle management, paid plans,
entitlements and payment processing through Stripe.

The implementation follows Domain-Driven Design (DDD) and separates
domain logic, application services, infrastructure adapters and REST
interfaces.

## Payment providers

| Provider configuration | Description |
|---|---|
| simulated | Local simulated payment processing |
| stripe | Stripe PaymentIntent integration |

The default provider is simulated.

## Billing configuration

| Property | Default |
|---|---|
| subscriptions.billing.payment-provider | simulated |
| subscriptions.billing.scheduler.enabled | false |
| subscriptions.billing.scheduler.delay-ms | 60000 |
| subscriptions.billing.simulated-renewal-outcome | CONFIRMED |
| subscriptions.billing.stripe.secret-key | Required for Stripe |
| subscriptions.billing.stripe.webhook-secret | Required for Stripe |

Stripe credentials must be supplied through external configuration.
Never commit Stripe secret keys or webhook signing secrets.

## REST API

Base URL: http://localhost:8080/api/v1

| Method | Endpoint | Responsibility |
|---|---|---|
| POST | /subscriptions | Request a subscription |
| POST | /subscriptions/{subscriptionId}/payment | Initiate activation payment |
| POST | /subscriptions/{subscriptionId}/renewal/payment | Initiate renewal payment |
| PATCH | /subscriptions/{subscriptionId}/plan | Change subscription plan |
| POST | /subscriptions/{subscriptionId}/cancellation | Schedule cancellation |
| GET | /subscriptions/subscriber/{subscriberId}/status | Retrieve subscription status |
| GET | /subscriptions/subscriber/{subscriberId}/entitlements | Retrieve entitlements |
| POST | /webhooks/stripe | Process signed Stripe events |

## Payment activation

1. Create a subscription request.
2. Initiate the activation payment.
3. Receive the Stripe PaymentIntent information.
4. Complete the payment through Stripe.
5. Process the verified successful payment webhook.
6. Activate the subscription and enable its entitlements.

Payment initiation alone does not activate a subscription.

## Subscription renewal

1. Initiate a renewal payment for an active subscription.
2. Reserve the payment operation for the current billing period.
3. Create or retrieve the corresponding Stripe PaymentIntent.
4. Complete the payment through Stripe.
5. Process the verified successful payment webhook.
6. Extend the subscription by its configured billing cycle.

Monthly subscriptions are extended by one calendar month.
Annual subscriptions are extended by one calendar year.

The new billing period is calculated from the previous period end.

## Payment idempotency

Activation operation key:

    guardian-plus:activation:{subscriptionId}

Renewal operation key:

    guardian-plus:renewal:{subscriptionId}:{currentPeriodEnd}

Repeated payment initiation requests reuse persisted Stripe PaymentIntent
references for the same operation.

When an external reference has not been registered, the Stripe adapter
uses a conservative retry window of 23 hours. Older unresolved operations
require reconciliation.

## Webhook processing

Supported Stripe events:

- payment_intent.succeeded
- payment_intent.payment_failed

Webhook processing validates:

- Stripe signature.
- Payment provider and external reference.
- Payment amount and currency.
- Subscription, subscriber and plan metadata.
- Payment operation purpose.
- Current billing period.
- Successful payment status and received amount.

Webhook deduplication key:

    {eventType}:{externalReference}

Subscriber locks, payment operation locks and transactional processing
protect against concurrent and duplicate processing.

## PostgreSQL persistence

Relevant tables:

- subscriptions
- subscription_entitlements
- subscription_payment_operations
- processed_payment_webhook_events

Payment operation references and processed webhook events are persisted
in PostgreSQL to support recovery after application restarts.

## Billing scheduler

The billing scheduler is disabled by default.

The scheduler currently supports only the simulated payment provider.

Starting the application with Stripe and the simulated billing scheduler
enabled is rejected.

The simulated renewal flow also rejects Stripe PaymentIntent results,
preventing simulated confirmation or failure handlers from being invoked
for those payments.

When Stripe is enabled, renewal is initiated manually through the REST API
and confirmed through a verified Stripe webhook.

Automatic recurring Stripe charges and automatic cancellation or
expiration through the billing scheduler are not supported in Stripe mode.

## Local Stripe testing

Use Stripe Sandbox and the Stripe CLI.

Keep the Stripe webhook listener running in a separate terminal:

    stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe

Configure the signing secret provided by the listener in the Spring Boot
environment before starting the application.

Use test credentials and test payment methods only.

Swagger UI:

    http://localhost:8080/swagger-ui/index.html

OpenAPI specification:

    http://localhost:8080/v3/api-docs

## Mobile integration

The Flutter and native Android applications should:

1. Request payment initiation from the backend.
2. Use the returned clientSecret with the Stripe client SDK when available.
3. Complete the payment through Stripe.
4. Refresh subscription status and entitlements from the backend.
5. Enable Premium features according to backend entitlement responses.

The client must never activate Premium features solely from a successful
client-side payment result.

Stripe secret keys and webhook signing secrets must remain on the backend.

## Validation results

Validated in the local development environment on November 26, 2026:

- Subscription lifecycle.
- PostgreSQL concurrency.
- Payment operation persistence.
- Recovery after application restart.
- Rejected activation payment and successful retry.
- Premium subscription activation.
- Premium entitlements.
- Stripe webhook persistence.
- Duplicate activation webhook processing.
- Renewal payment initiation idempotency.
- Successful Stripe Sandbox renewal.
- Duplicate renewal webhook processing.
- Simulated renewal provider isolation.
- Invalid scheduler configuration rejection.

Regression suite: 490 tests passed before documentation changes.

## Running tests

Windows:

    .\mvnw.cmd test

Linux and macOS:

    ./mvnw test

## Current limitations

- Stripe renewal currently requires explicit payment initiation.
- Automatic off-session recurring charges are not implemented.
- The simulated scheduler cannot be enabled in Stripe mode.
- Automatic cancellation and expiration through that scheduler are
  unavailable in Stripe mode.
- Production payment configuration and endpoint access control require
  deployment-specific verification.
- Flutter and native Android integration remain pending.
