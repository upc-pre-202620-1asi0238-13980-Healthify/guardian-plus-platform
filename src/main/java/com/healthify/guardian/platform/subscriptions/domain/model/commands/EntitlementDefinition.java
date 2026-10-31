package com.healthify.guardian.platform.subscriptions.domain.model.commands;

public record EntitlementDefinition(
        String code,
        String name,
        String description
) {
}