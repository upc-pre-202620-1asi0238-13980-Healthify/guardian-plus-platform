package com.healthify.guardian.platform.subscriptions.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record PlanResource(
        UUID id,
        String name,
        BigDecimal price,
        String currency,
        String billingCycle,
        boolean active,
        boolean requiresPayment,
        Set<String> entitlementCodes
) {
}