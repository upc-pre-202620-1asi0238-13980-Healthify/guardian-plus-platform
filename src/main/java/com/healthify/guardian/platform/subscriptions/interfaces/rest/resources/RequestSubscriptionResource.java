package com.healthify.guardian.platform.subscriptions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(
        description = "Data required to request a Guardian+ subscription"
)
public record RequestSubscriptionResource(

        @NotNull(message = "subscriberId is required")
        @Schema(
                description = "Subscriber identifier",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID subscriberId,

        @NotNull(message = "planId is required")
        @Schema(
                description = "Subscription plan identifier",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID planId
) {
}