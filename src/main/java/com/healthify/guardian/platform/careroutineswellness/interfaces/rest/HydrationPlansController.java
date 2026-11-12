package com.healthify.guardian.platform.careroutineswellness.interfaces.rest;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.HydrationPlanCommandService;
import com.healthify.guardian.platform.careroutineswellness.application.queryservices.HydrationPlanQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetHydrationPlanByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetHydrationProgressQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ConfigureHydrationPlanResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.HydrationPlanResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ConfigureHydrationPlanCommandFromResourceAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.HydrationPlanResourceFromEntityAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the hydration plan of a person under care and today's progress towards its goal.
 */
@RestController
@RequestMapping(value = "/api/v1/hydration-plans", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Hydration Plans", description = "Hydration goal, periodic reminders and daily progress endpoints")
public class HydrationPlansController {

    private final HydrationPlanCommandService hydrationPlanCommandService;
    private final HydrationPlanQueryService hydrationPlanQueryService;
    private final SleepWindow sleepWindow;
    private final ZoneId zone;

    public HydrationPlansController(
            HydrationPlanCommandService hydrationPlanCommandService,
            HydrationPlanQueryService hydrationPlanQueryService,
            SleepWindow careRoutinesWellnessDefaultSleepWindow,
            ZoneId careRoutinesWellnessZoneId) {
        this.hydrationPlanCommandService = hydrationPlanCommandService;
        this.hydrationPlanQueryService = hydrationPlanQueryService;
        this.sleepWindow = careRoutinesWellnessDefaultSleepWindow;
        this.zone = careRoutinesWellnessZoneId;
    }

    @GetMapping("/citizen/{personUnderCareId}")
    @Operation(
            summary = "Get the hydration plan of a person under care",
            description = "Retrieves the hydration plan together with today's progress: glasses consumed (confirmed " +
                    "hydration reminders), glasses left to reach the goal and when the next reminder is due."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Hydration plan found",
                    content = @Content(schema = @Schema(implementation = HydrationPlanResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "No hydration plan configured yet")
    })
    public ResponseEntity<?> getHydrationPlan(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId
    ) {
        var plan = hydrationPlanQueryService.handle(
                new GetHydrationPlanByPersonUnderCareIdQuery(new PersonUnderCareId(personUnderCareId)));
        if (plan.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("HydrationPlan", personUnderCareId.toString()));
        }
        return ResponseEntity.ok(toResource(plan.get()));
    }

    @PutMapping("/citizen/{personUnderCareId}")
    @Operation(
            summary = "Configure the hydration plan of a person under care",
            description = "Creates or changes the hydration plan. Switching it on or off, or changing the interval, " +
                    "replaces the pending hydration reminders; reminders inside the sleep window are suppressed " +
                    "while respectSleepWindow is true."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Hydration plan configured successfully",
                    content = @Content(schema = @Schema(implementation = HydrationPlanResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> configureHydrationPlan(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId,
            @Valid @RequestBody ConfigureHydrationPlanResource resource
    ) {
        var command = ConfigureHydrationPlanCommandFromResourceAssembler.toCommandFromResource(personUnderCareId, resource);
        var result = hydrationPlanCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result, this::toResource, HttpStatus.OK);
    }

    private HydrationPlanResource toResource(HydrationPlan plan) {
        var progress = hydrationPlanQueryService.handle(
                new GetHydrationProgressQuery(plan.getPersonUnderCareId(), LocalDate.now(zone)));
        return HydrationPlanResourceFromEntityAssembler.toResourceFromEntity(plan, progress, sleepWindow);
    }
}
