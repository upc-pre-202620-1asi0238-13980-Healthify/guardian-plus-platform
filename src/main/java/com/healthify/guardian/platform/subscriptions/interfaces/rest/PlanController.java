package com.healthify.guardian.platform.subscriptions.interfaces.rest;

import com.healthify.guardian.platform.subscriptions.application.queryservices.PlanQueryService;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetActivePlansQuery;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.PlanResource;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.transform.PlanResourceFromEntityAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions/plans")
@Tag(
        name = "Subscription Plans",
        description = "Guardian+ subscription plan catalog"
)
public class PlanController {

    private final PlanQueryService planQueryService;

    public PlanController(
            PlanQueryService planQueryService
    ) {

        this.planQueryService = planQueryService;
    }

    @GetMapping
    @Operation(
            summary = "Get active subscription plans",
            description = """
                    Returns the available subscription plans,
                    including their prices, currencies,
                    billing cycles and entitlement codes.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Active subscription plans retrieved"
            )
    })
    public ResponseEntity<List<PlanResource>> getActivePlans() {

        var plans =
                planQueryService.handle(
                        new GetActivePlansQuery()
                );

        var resources =
                plans.stream()
                        .map(
                                PlanResourceFromEntityAssembler
                                        ::toResourceFromEntity
                        )
                        .sorted(
                                Comparator.comparing(PlanResource::price)
                                        .thenComparing(PlanResource::name)
                        )
                        .toList();

        return ResponseEntity.ok(resources);
    }
}