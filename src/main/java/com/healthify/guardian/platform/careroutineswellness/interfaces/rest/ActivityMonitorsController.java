package com.healthify.guardian.platform.careroutineswellness.interfaces.rest;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ActivityMonitorCommandService;
import com.healthify.guardian.platform.careroutineswellness.application.queryservices.ActivityMonitorQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetActivityLogEntriesByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetActivityMonitorByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ActivityMonitorResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ConfigureInactivityDetectionResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ActivityLogEntryResourceFromEntityAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ActivityMonitorResourceFromEntityAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ConfigureInactivityDetectionCommandFromResourceAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the physical-activity status of a person under care: current inactivity counter,
 * prolonged-inactivity detection settings and recent activity.
 */
@RestController
@RequestMapping(value = "/api/v1/activity-monitors", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Activity Monitors", description = "Inactivity detection and recent activity endpoints")
public class ActivityMonitorsController {

    private static final int MAX_LOG_ENTRIES = 100;

    private final ActivityMonitorCommandService activityMonitorCommandService;
    private final ActivityMonitorQueryService activityMonitorQueryService;
    private final SleepWindow sleepWindow;

    public ActivityMonitorsController(
            ActivityMonitorCommandService activityMonitorCommandService,
            ActivityMonitorQueryService activityMonitorQueryService,
            SleepWindow careRoutinesWellnessDefaultSleepWindow) {
        this.activityMonitorCommandService = activityMonitorCommandService;
        this.activityMonitorQueryService = activityMonitorQueryService;
        this.sleepWindow = careRoutinesWellnessDefaultSleepWindow;
    }

    @GetMapping("/citizen/{personUnderCareId}")
    @Operation(
            summary = "Get the activity status of a person under care",
            description = "Retrieves whether prolonged inactivity is currently flagged, the current minutes without " +
                    "movement, when movement was last detected and how inactivity is being watched."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Activity monitor found",
                    content = @Content(schema = @Schema(implementation = ActivityMonitorResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "No activity telemetry or configuration received yet")
    })
    public ResponseEntity<?> getActivityMonitor(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId
    ) {
        var monitor = activityMonitorQueryService.handle(
                new GetActivityMonitorByPersonUnderCareIdQuery(new PersonUnderCareId(personUnderCareId)));
        if (monitor.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("ActivityMonitor", personUnderCareId.toString()));
        }
        return ResponseEntity.ok(toResource(monitor.get()));
    }

    @PutMapping("/citizen/{personUnderCareId}/inactivity-detection")
    @Operation(
            summary = "Configure prolonged-inactivity detection",
            description = "Turns prolonged-inactivity detection on or off and sets after how many minutes without " +
                    "movement (30 to 240) an alert is raised. Detection only applies outside the sleep window."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detection configured successfully",
                    content = @Content(schema = @Schema(implementation = ActivityMonitorResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> configureInactivityDetection(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId,
            @Valid @RequestBody ConfigureInactivityDetectionResource resource
    ) {
        var command = ConfigureInactivityDetectionCommandFromResourceAssembler.toCommandFromResource(personUnderCareId, resource);
        var result = activityMonitorCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result, this::toResource, HttpStatus.OK);
    }

    @GetMapping("/citizen/{personUnderCareId}/log-entries")
    @Operation(
            summary = "Get the recent activity of a person under care",
            description = "Retrieves the most recent noteworthy activity facts, newest first: movement after a long " +
                    "still period, finished walks and prolonged-inactivity detections."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity log retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid limit")
    })
    public ResponseEntity<?> getActivityLogEntries(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "Maximum number of entries, 1 to " + MAX_LOG_ENTRIES)
            int limit
    ) {
        if (limit < 1 || limit > MAX_LOG_ENTRIES) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                    "activity-log-entries",
                    MessageResolver.resolveOrDefault("activity-log-entry.limit.invalid", "Invalid limit")));
        }
        var query = new GetActivityLogEntriesByPersonUnderCareIdQuery(new PersonUnderCareId(personUnderCareId), limit);
        return ResponseEntity.ok(activityMonitorQueryService.handle(query).stream()
                .map(ActivityLogEntryResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    private ActivityMonitorResource toResource(ActivityMonitor monitor) {
        return ActivityMonitorResourceFromEntityAssembler.toResourceFromEntity(monitor, sleepWindow);
    }
}
