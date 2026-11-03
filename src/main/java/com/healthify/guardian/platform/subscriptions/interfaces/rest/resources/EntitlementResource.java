package com.healthify.guardian.platform.subscriptions.interfaces.rest.resources;

public record EntitlementResource(
        String code,
        String name,
        String description
) {
}