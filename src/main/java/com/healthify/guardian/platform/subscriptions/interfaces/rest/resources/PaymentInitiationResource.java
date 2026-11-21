package com.healthify.guardian.platform.subscriptions.interfaces.rest.resources;

public record PaymentInitiationResource(
        String provider,
        String externalReference,
        String status,
        String clientSecret
) {
}