package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignThresholdCommandService;
import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignThresholdQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.ActivateVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DeactivateVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.DefineVitalSignThresholdResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignThresholdResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.DefineVitalSignThresholdCommandFromResourceAssembler;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.VitalSignThresholdResourceFromEntityAssembler;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.ErrorResource;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the clinical ranges each care recipient is evaluated against (US09, US12).
 */
@RestController
@RequestMapping(value = "/api/v1/vital-sign-thresholds", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Vital Sign Thresholds", description = "Per care recipient clinical range configuration endpoints")
public class VitalSignThresholdsController {

    private final VitalSignThresholdCommandService vitalSignThresholdCommandService;
    private final VitalSignThresholdQueryService vitalSignThresholdQueryService;

    public VitalSignThresholdsController(VitalSignThresholdCommandService vitalSignThresholdCommandService,
                                         VitalSignThresholdQueryService vitalSignThresholdQueryService) {
        this.vitalSignThresholdCommandService = vitalSignThresholdCommandService;
        this.vitalSignThresholdQueryService = vitalSignThresholdQueryService;
    }

    @GetMapping("/care-recipient/{careRecipientProfileId}")
    @Operation(summary = "List active thresholds", description = "Lists the thresholds in force for a care recipient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thresholds retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = VitalSignThresholdResource.class))))
    })
    public ResponseEntity<List<VitalSignThresholdResource>> getActiveThresholds(
            @PathVariable @Parameter(description = "Care recipient profile unique identifier", required = true)
            UUID careRecipientProfileId) {
        var query = new GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery(new CareRecipientProfileId(careRecipientProfileId));
        return ResponseEntity.ok(vitalSignThresholdQueryService.handle(query).stream()
                .map(VitalSignThresholdResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @PutMapping
    @Operation(
            summary = "Define a threshold",
            description = "Defines the clinical range (minimum, maximum, consecutive readings required) of a vital sign " +
                    "type for a care recipient, or redefines and reactivates the existing one."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Threshold defined",
                    content = @Content(schema = @Schema(implementation = VitalSignThresholdResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid range", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "Vital sign type not found", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> defineThreshold(@Valid @RequestBody DefineVitalSignThresholdResource resource) {
        var result = vitalSignThresholdCommandService.handle(
                DefineVitalSignThresholdCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, VitalSignThresholdResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping("/{thresholdId}/activate")
    @Operation(summary = "Activate a threshold", description = "Puts a deactivated threshold back in force.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Threshold activated",
                    content = @Content(schema = @Schema(implementation = VitalSignThresholdResource.class))),
            @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Threshold already active", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> activateThreshold(
            @PathVariable @Parameter(description = "Threshold unique identifier", required = true) UUID thresholdId) {
        var result = vitalSignThresholdCommandService.handle(new ActivateVitalSignThresholdCommand(thresholdId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, VitalSignThresholdResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @DeleteMapping("/{thresholdId}")
    @Operation(summary = "Deactivate a threshold", description = "Stops evaluating readings against a threshold; it is kept for history.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Threshold deactivated",
                    content = @Content(schema = @Schema(implementation = VitalSignThresholdResource.class))),
            @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Threshold already inactive", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> deactivateThreshold(
            @PathVariable @Parameter(description = "Threshold unique identifier", required = true) UUID thresholdId) {
        var result = vitalSignThresholdCommandService.handle(new DeactivateVitalSignThresholdCommand(thresholdId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, VitalSignThresholdResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
