package com.healthify.guardian.platform.emergencyalerting.interfaces.rest;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertSettingsCommandService;
import com.healthify.guardian.platform.emergencyalerting.application.queryservices.AlertSettingsQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ActivateSilentModeCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DeactivateSilentModeCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertSettingsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertSettingsResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.UpdateAlertSettingsResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.AlertSettingsResourceFromEntityAssembler;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.UpdateAlertSettingsCommandFromResourceAssembler;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the alerting configuration of a person under care.
 */
@RestController
@RequestMapping(value = "/api/v1/alert-settings", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Alert Settings", description = "Acknowledgement timeout, escalation and silent mode of a person under care")
public class AlertSettingsController {

    private final AlertSettingsCommandService alertSettingsCommandService;
    private final AlertSettingsQueryService alertSettingsQueryService;

    public AlertSettingsController(
            AlertSettingsCommandService alertSettingsCommandService,
            AlertSettingsQueryService alertSettingsQueryService) {
        this.alertSettingsCommandService = alertSettingsCommandService;
        this.alertSettingsQueryService = alertSettingsQueryService;
    }

    @GetMapping("/care-recipient/{careRecipientProfileId}")
    @Operation(
            summary = "Get the alert settings",
            description = "Retrieves the alerting configuration, or the defaults (60 s, escalation on, silent mode " +
                    "off, critical alerts escalate) if it was never changed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alert settings retrieved",
                    content = @Content(schema = @Schema(implementation = AlertSettingsResource.class))
            )
    })
    public ResponseEntity<AlertSettingsResource> getAlertSettings(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID careRecipientProfileId
    ) {
        var query = new GetAlertSettingsByCareRecipientProfileIdQuery(
                new CareRecipientProfileId(careRecipientProfileId));
        return ResponseEntity.ok(
                AlertSettingsResourceFromEntityAssembler.toResourceFromEntity(alertSettingsQueryService.handle(query)));
    }

    @PutMapping("/care-recipient/{careRecipientProfileId}")
    @Operation(
            summary = "Update the alert settings",
            description = "Updates the primary contact's acknowledgement timeout, whether unacknowledged alerts " +
                    "escalate, and whether critical alerts reach every contact at once."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alert settings updated",
                    content = @Content(schema = @Schema(implementation = AlertSettingsResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> updateAlertSettings(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID careRecipientProfileId,
            @Valid @RequestBody UpdateAlertSettingsResource resource
    ) {
        var command = UpdateAlertSettingsCommandFromResourceAssembler
                .toCommandFromResource(careRecipientProfileId, resource);
        var result = alertSettingsCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertSettingsResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping("/care-recipient/{careRecipientProfileId}/silent-mode")
    @Operation(summary = "Activate silent mode", description = "From now on only critical alerts may be audible.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Silent mode activated",
                    content = @Content(schema = @Schema(implementation = AlertSettingsResource.class))
            )
    })
    public ResponseEntity<?> activateSilentMode(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID careRecipientProfileId
    ) {
        var result = alertSettingsCommandService.handle(new ActivateSilentModeCommand(careRecipientProfileId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertSettingsResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @DeleteMapping("/care-recipient/{careRecipientProfileId}/silent-mode")
    @Operation(summary = "Deactivate silent mode", description = "Every alert may be audible again.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Silent mode deactivated",
                    content = @Content(schema = @Schema(implementation = AlertSettingsResource.class))
            )
    })
    public ResponseEntity<?> deactivateSilentMode(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID careRecipientProfileId
    ) {
        var result = alertSettingsCommandService.handle(new DeactivateSilentModeCommand(careRecipientProfileId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertSettingsResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
