package com.healthify.guardian.platform.emergencyalerting.interfaces.rest;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.IncidentCommandService;
import com.healthify.guardian.platform.emergencyalerting.application.queryservices.IncidentQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.CloseIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.StabilizeIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetIncidentByAlertIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetIncidentByIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.CloseIncidentResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.IncidentResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.StabilizeIncidentResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.IncidentResourceFromEntityAssembler;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for incident endpoints.
 */
@RestController
@RequestMapping(value = "/api/v1/incidents", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Incidents", description = "Attention of acknowledged alerts, from in attention to closed")
public class IncidentsController {

    private final IncidentCommandService incidentCommandService;
    private final IncidentQueryService incidentQueryService;

    public IncidentsController(
            IncidentCommandService incidentCommandService, IncidentQueryService incidentQueryService) {
        this.incidentCommandService = incidentCommandService;
        this.incidentQueryService = incidentQueryService;
    }

    @GetMapping("/{incidentId}")
    @Operation(summary = "Get an incident", description = "Retrieves an incident by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident found",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<?> getIncidentById(
            @PathVariable
            @Parameter(description = "Incident unique identifier", required = true)
            UUID incidentId
    ) {
        return toResponse(incidentQueryService.handle(new GetIncidentByIdQuery(new IncidentId(incidentId))),
                incidentId);
    }

    @GetMapping("/alert/{alertId}")
    @Operation(summary = "Get the incident of an alert", description = "Retrieves the incident opened for an alert.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident found",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "The alert has no incident")
    })
    public ResponseEntity<?> getIncidentByAlertId(
            @PathVariable
            @Parameter(description = "Alert unique identifier", required = true)
            UUID alertId
    ) {
        return toResponse(incidentQueryService.handle(new GetIncidentByAlertIdQuery(new AlertId(alertId))), alertId);
    }

    @PostMapping("/{incidentId}/stabilize")
    @Operation(
            summary = "Stabilize an incident",
            description = "Declares that the situation of the person under care is under control."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident stabilized",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Incident not found"),
            @ApiResponse(responseCode = "422", description = "Incident is not in attention")
    })
    public ResponseEntity<?> stabilizeIncident(
            @PathVariable
            @Parameter(description = "Incident unique identifier", required = true)
            UUID incidentId,
            @Valid @RequestBody StabilizeIncidentResource resource
    ) {
        var result = incidentCommandService.handle(new StabilizeIncidentCommand(incidentId, resource.notes()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, IncidentResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping("/{incidentId}/close")
    @Operation(
            summary = "Close an incident",
            description = "Definitively closes the incident, which also resolves its alert."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident closed",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Incident not found"),
            @ApiResponse(responseCode = "422", description = "Incident is already closed")
    })
    public ResponseEntity<?> closeIncident(
            @PathVariable
            @Parameter(description = "Incident unique identifier", required = true)
            UUID incidentId,
            @Valid @RequestBody CloseIncidentResource resource
    ) {
        var result = incidentCommandService.handle(new CloseIncidentCommand(incidentId, resource.notes()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, IncidentResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    private static ResponseEntity<?> toResponse(
            Optional<Incident> incident, UUID identifier) {
        if (incident.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Incident", identifier.toString()));
        }
        return ResponseEntity.ok(IncidentResourceFromEntityAssembler.toResourceFromEntity(incident.get()));
    }
}
