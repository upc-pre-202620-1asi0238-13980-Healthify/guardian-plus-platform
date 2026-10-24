package com.healthify.guardian.platform.subscriptions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(
        description = "Data required to change the current subscription plan"
)
public record ChangeSubscriptionPlanResource(

        @NotNull(message = "newPlanId is required")
        @Schema(
                description = "Identifier of the new subscription plan",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID newPlanId
) {
}