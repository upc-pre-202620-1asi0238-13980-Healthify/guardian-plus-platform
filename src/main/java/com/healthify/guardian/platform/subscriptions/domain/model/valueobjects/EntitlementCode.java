package com.healthify.guardian.platform.subscriptions.domain.model.valueobjects;

public record EntitlementCode(String value) {

    public EntitlementCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "subscription.entitlement-code.invalid");
        }

        value = value.strip().toUpperCase();
    }
}