package com.healthify.guardian.platform.subscriptions.interfaces.rest;

import com.healthify.guardian.platform.subscriptions.application.commandservices.SubscriptionCommandService;
import com.healthify.guardian.platform.subscriptions.application.queryservices.SubscriptionApplicationQueryService;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ApplyPlanChangeCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCancellationCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetAvailableEntitlementsQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetSubscriptionStatusQuery;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.ChangeSubscriptionPlanResource;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.EntitlementSetResource;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.RequestSubscriptionResource;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.SubscriptionResource;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.transform.EntitlementSetResourceFromEntityAssembler;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(
        name = "Subscriptions",
        description = "Guardian+ subscription lifecycle operations"
)
public class SubscriptionController {

    private final SubscriptionCommandService
            subscriptionCommandService;

    private final SubscriptionApplicationQueryService
            subscriptionQueryService;

    public SubscriptionController(
            SubscriptionCommandService subscriptionCommandService,
            SubscriptionApplicationQueryService subscriptionQueryService
    ) {
        this.subscriptionCommandService =
                subscriptionCommandService;

        this.subscriptionQueryService =
                subscriptionQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Request a subscription",
            description = "Creates a subscription request in PENDING status"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Subscription request created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or business rule violation"
            )
    })
    public ResponseEntity<SubscriptionResource> requestSubscription(
            @Valid
            @RequestBody
            RequestSubscriptionResource resource
    ) {

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCommand(
                                resource.subscriberId(),
                                resource.planId()
                        )
                );

        if (result.isFailure()) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        var subscription =
                result.toOptional()
                        .orElseThrow();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        SubscriptionResourceFromEntityAssembler
                                .toResourceFromEntity(
                                        subscription
                                )
                );
    }

    @PatchMapping("/{subscriptionId}/plan")
    @Operation(
            summary = "Change subscription plan",
            description = "Changes the plan of an active subscription"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan changed"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid plan change"
            )
    })
    public ResponseEntity<SubscriptionResource> changePlan(
            @PathVariable UUID subscriptionId,

            @Valid
            @RequestBody
            ChangeSubscriptionPlanResource resource
    ) {

        var result =
                subscriptionCommandService.handle(
                        new ApplyPlanChangeCommand(
                                subscriptionId,
                                resource.newPlanId()
                        )
                );

        if (result.isFailure()) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        var subscription =
                result.toOptional()
                        .orElseThrow();

        return ResponseEntity.ok(
                SubscriptionResourceFromEntityAssembler
                        .toResourceFromEntity(
                                subscription
                        )
        );
    }

    @PostMapping("/{subscriptionId}/cancellation")
    @Operation(
            summary = "Request subscription cancellation",
            description = """
                    Schedules an active subscription for cancellation
                    at the end of its current billing period.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cancellation scheduled"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cancellation cannot be scheduled"
            )
    })
    public ResponseEntity<SubscriptionResource> requestCancellation(
            @PathVariable UUID subscriptionId
    ) {

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCancellationCommand(
                                subscriptionId
                        )
                );

        if (result.isFailure()) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        var subscription =
                result.toOptional()
                        .orElseThrow();

        return ResponseEntity.ok(
                SubscriptionResourceFromEntityAssembler
                        .toResourceFromEntity(
                                subscription
                        )
        );
    }

    @GetMapping("/subscriber/{subscriberId}/status")
    @Operation(
            summary = "Get current subscription",
            description = """
                    Returns the subscriber's currently active
                    or cancellation-scheduled subscription.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current subscription found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No current subscription found"
            )
    })
    public ResponseEntity<SubscriptionResource> getSubscriptionStatus(
            @PathVariable UUID subscriberId
    ) {

        var result =
                subscriptionQueryService.handle(
                        new GetSubscriptionStatusQuery(
                                subscriberId
                        )
                );

        if (result.isFailure()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        var subscription =
                result.toOptional()
                        .orElseThrow();

        return ResponseEntity.ok(
                SubscriptionResourceFromEntityAssembler
                        .toResourceFromEntity(
                                subscription
                        )
        );
    }

    @GetMapping("/subscriber/{subscriberId}/entitlements")
    @Operation(
            summary = "Get subscription entitlements",
            description = """
                    Returns the effective entitlements enabled
                    by the subscriber's current subscription.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entitlements found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Current subscription or entitlements not found"
            )
    })
    public ResponseEntity<EntitlementSetResource> getAvailableEntitlements(
            @PathVariable UUID subscriberId
    ) {

        var result =
                subscriptionQueryService.handle(
                        new GetAvailableEntitlementsQuery(
                                subscriberId
                        )
                );

        if (result.isFailure()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        var entitlementSet =
                result.toOptional()
                        .orElseThrow();

        return ResponseEntity.ok(
                EntitlementSetResourceFromEntityAssembler
                        .toResourceFromEntity(
                                entitlementSet
                        )
        );
    }
}