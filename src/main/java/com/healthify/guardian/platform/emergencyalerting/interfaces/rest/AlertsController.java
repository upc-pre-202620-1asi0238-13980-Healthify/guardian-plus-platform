package com.healthify.guardian.platform.emergencyalerting.interfaces.rest;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.application.queryservices.AlertQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ClaimAlertResponseCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.CompleteAlertResponseCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DismissAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetActiveAlertsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertByIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertHistoryByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetPendingAlertsByRecipientUserIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AcknowledgeAlertResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertSummaryResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.ClaimAlertResponseResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.CompleteAlertResponseResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.TriggerAlertResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.AcknowledgeAlertCommandFromResourceAssembler;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.AlertResourceFromEntityAssembler;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.TriggerAlertCommandFromResourceAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.PageResource;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for alert endpoints.
 */
@RestController
@RequestMapping(value = "/api/v1/alerts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Alerts", description = "Emergency alert triggering, acknowledgement and response endpoints")
public class AlertsController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;
    private final Clock clock;
    private final boolean allowAnyAlertSource;

    public AlertsController(
            AlertCommandService alertCommandService,
            AlertQueryService alertQueryService,
            Clock emergencyAlertingClock,
            @Value("${emergency-alerting.api.allow-any-alert-source:false}") boolean allowAnyAlertSource) {
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
        this.clock = emergencyAlertingClock;
        this.allowAnyAlertSource = allowAnyAlertSource;
    }

    @PostMapping
    @Operation(
            summary = "Trigger an alert",
            description = "Called by the wearable gateway when a fall is detected or the SOS button is pressed. " +
                    "A fall waits 20 s for the person to cancel it; an SOS is dispatched right away. Retrying the same " +
                    "signal returns the alert already active instead of raising a new one. Other source types are " +
                    "raised by the bounded contexts that detect them and are only accepted here in development."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Alert triggered (or already active for that signal)",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data or source type not accepted")
    })
    public ResponseEntity<?> triggerAlert(@Valid @RequestBody TriggerAlertResource resource) {
        if (!allowAnyAlertSource && !resource.sourceType().isWearableReported()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                    "trigger-alert", MessageResolver.resolveOrDefault("alert.source-type.not-allowed",
                            "Only FALL_DETECTED and SOS_TRIGGERED alerts can be triggered through this endpoint")));
        }
        var command = TriggerAlertCommandFromResourceAssembler.toCommandFromResource(resource, clock.instant());
        var result = alertCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get the alert history of a person under care",
            description = "Pages through every alert of a person under care, most recent first, optionally filtered " +
                    "by severity and by triggering period (ISO-8601 instants)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert history page retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filters or paging")
    })
    public ResponseEntity<?> getAlertHistory(
            @RequestParam
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID careRecipientProfileId,
            @RequestParam(required = false)
            @Parameter(description = "Only alerts of this severity")
            Severity severity,
            @RequestParam(required = false)
            @Parameter(description = "Only alerts triggered at or after this instant, e.g. 2026-10-01T00:00:00Z")
            Instant from,
            @RequestParam(required = false)
            @Parameter(description = "Only alerts triggered at or before this instant")
            Instant to,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Zero-based page index")
            int page,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "Page size, 1 to " + MAX_PAGE_SIZE)
            int size
    ) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                    "alert-history", MessageResolver.resolveOrDefault("alert.page.invalid", "Invalid page or size")));
        }
        var query = new GetAlertHistoryByCareRecipientProfileIdQuery(
                new CareRecipientProfileId(careRecipientProfileId), new DateRange(from, to), severity, page, size);
        return ResponseEntity.ok(PageResource.from(
                alertQueryService.handle(query), AlertResourceFromEntityAssembler::toSummaryResourceFromEntity));
    }

    @GetMapping("/active/care-recipient/{careRecipientProfileId}")
    @Operation(
            summary = "List the active alerts of a person under care",
            description = "Retrieves every alert of the person under care that is neither dismissed nor resolved."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active alerts retrieved successfully")
    })
    public ResponseEntity<List<AlertSummaryResource>> getActiveAlerts(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID careRecipientProfileId
    ) {
        var query = new GetActiveAlertsByCareRecipientProfileIdQuery(
                new CareRecipientProfileId(careRecipientProfileId));
        return ResponseEntity.ok(alertQueryService.handle(query).stream()
                .map(AlertResourceFromEntityAssembler::toSummaryResourceFromEntity)
                .toList());
    }

    @GetMapping("/pending/recipient/{userId}")
    @Operation(
            summary = "List the alerts pending acknowledgement by a Care Circle member",
            description = "Retrieves the alerts the member was notified about and that nobody has acknowledged yet. " +
                    "The mobile app polls this endpoint to show in-app alerts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending alerts retrieved successfully")
    })
    public ResponseEntity<List<AlertSummaryResource>> getPendingAlerts(
            @PathVariable
            @Parameter(description = "Care Circle member unique identifier", required = true)
            UUID userId
    ) {
        var query = new GetPendingAlertsByRecipientUserIdQuery(new UserId(userId));
        return ResponseEntity.ok(alertQueryService.handle(query).stream()
                .map(AlertResourceFromEntityAssembler::toSummaryResourceFromEntity)
                .toList());
    }

    @GetMapping("/{alertId}")
    @Operation(
            summary = "Get an alert",
            description = "Retrieves an alert with its deliveries and the Care Circle's responses."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alert found",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    public ResponseEntity<?> getAlertById(
            @PathVariable
            @Parameter(description = "Alert unique identifier", required = true)
            UUID alertId
    ) {
        var alert = alertQueryService.handle(new GetAlertByIdQuery(new AlertId(alertId)));
        if (alert.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Alert", alertId.toString()));
        }
        return ResponseEntity.ok(AlertResourceFromEntityAssembler.toResourceFromEntity(alert.get()));
    }

    @PostMapping("/{alertId}/dismiss")
    @Operation(
            summary = "Dismiss a fall as a false positive",
            description = "Cancels a detected fall within its 20 s confirmation window, so it is never dispatched."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alert dismissed",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "422", description = "Alert is not pending confirmation or its window expired")
    })
    public ResponseEntity<?> dismissAlert(
            @PathVariable
            @Parameter(description = "Alert unique identifier", required = true)
            UUID alertId
    ) {
        var result = alertCommandService.handle(new DismissAlertCommand(alertId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping("/{alertId}/acknowledge")
    @Operation(
            summary = "Acknowledge an alert",
            description = "Records that a notified Care Circle member saw the alert. This stops its escalation and " +
                    "opens its incident."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alert acknowledged",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "422", description = "Alert is not awaiting acknowledgement or the user was not notified")
    })
    public ResponseEntity<?> acknowledgeAlert(
            @PathVariable
            @Parameter(description = "Alert unique identifier", required = true)
            UUID alertId,
            @Valid @RequestBody AcknowledgeAlertResource resource
    ) {
        var command = AcknowledgeAlertCommandFromResourceAssembler.toCommandFromResource(alertId, resource);
        var result = alertCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping("/{alertId}/responses")
    @Operation(
            summary = "Take charge of responding to an alert",
            description = "Records that a notified Care Circle member is on their way. It also acknowledges the alert " +
                    "if nobody had, and tells the rest of the Care Circle. Only one response may be in progress."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Response claimed",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "422", description = "Someone is already responding or the user was not notified")
    })
    public ResponseEntity<?> claimAlertResponse(
            @PathVariable
            @Parameter(description = "Alert unique identifier", required = true)
            UUID alertId,
            @Valid @RequestBody ClaimAlertResponseResource resource
    ) {
        var result = alertCommandService.handle(new ClaimAlertResponseCommand(alertId, resource.responderUserId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PostMapping("/{alertId}/responses/{responseId}/complete")
    @Operation(
            summary = "Complete a response",
            description = "Records the outcome of the intervention of the Care Circle member who took charge."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Response completed",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data or unknown response"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "422", description = "Response is not in progress")
    })
    public ResponseEntity<?> completeAlertResponse(
            @PathVariable
            @Parameter(description = "Alert unique identifier", required = true)
            UUID alertId,
            @PathVariable
            @Parameter(description = "Response unique identifier", required = true)
            UUID responseId,
            @Valid @RequestBody CompleteAlertResponseResource resource
    ) {
        var result = alertCommandService.handle(
                new CompleteAlertResponseCommand(alertId, responseId, resource.notes()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
