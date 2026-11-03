package com.healthify.guardian.platform.subscriptions.domain.model.queries;

import java.util.UUID;

public record GetAvailableEntitlementsQuery(
        UUID subscriberId
) {
}