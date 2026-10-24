package com.healthify.guardian.platform.emergencyalerting.interfaces.rest;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertChannelSettingCommandService;
import com.healthify.guardian.platform.emergencyalerting.application.queryservices.AlertChannelSettingQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertChannelSettingsByUserIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertChannelSettingResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.ConfigureAlertChannelResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.AlertChannelSettingResourceFromEntityAssembler;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.ConfigureAlertChannelCommandFromResourceAssembler;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the notification channels of a Care Circle member.
 */
@RestController
@RequestMapping(value = "/api/v1/alert-channel-settings", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Alert Channel Settings", description = "Notification channels each Care Circle member receives alerts through")
public class AlertChannelSettingsController {

    private final AlertChannelSettingCommandService alertChannelSettingCommandService;
    private final AlertChannelSettingQueryService alertChannelSettingQueryService;

    public AlertChannelSettingsController(
            AlertChannelSettingCommandService alertChannelSettingCommandService,
            AlertChannelSettingQueryService alertChannelSettingQueryService) {
        this.alertChannelSettingCommandService = alertChannelSettingCommandService;
        this.alertChannelSettingQueryService = alertChannelSettingQueryService;
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "List the notification channels of a Care Circle member",
            description = "Retrieves the configured channels. A member with no channel configured receives in-app " +
                    "alerts; critical alerts and broadcasts are always backed up by SMS."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Channels retrieved successfully")
    })
    public ResponseEntity<List<AlertChannelSettingResource>> getAlertChannelSettings(
            @PathVariable
            @Parameter(description = "Care Circle member unique identifier", required = true)
            UUID userId
    ) {
        var query = new GetAlertChannelSettingsByUserIdQuery(new UserId(userId));
        return ResponseEntity.ok(alertChannelSettingQueryService.handle(query).stream()
                .map(AlertChannelSettingResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @PutMapping("/user/{userId}/channels/{channel}")
    @Operation(
            summary = "Enable or disable a notification channel",
            description = "Enables or disables a channel and, for PUSH, registers the device token. The member must " +
                    "keep at least one channel enabled."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Channel configured",
                    content = @Content(schema = @Schema(implementation = AlertChannelSettingResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data or token for a non-PUSH channel"),
            @ApiResponse(responseCode = "422", description = "It would leave the member without enabled channels")
    })
    public ResponseEntity<?> configureAlertChannel(
            @PathVariable
            @Parameter(description = "Care Circle member unique identifier", required = true)
            UUID userId,
            @PathVariable
            @Parameter(description = "PUSH, SMS or IN_APP", required = true)
            NotificationChannel channel,
            @Valid @RequestBody ConfigureAlertChannelResource resource
    ) {
        var command = ConfigureAlertChannelCommandFromResourceAssembler
                .toCommandFromResource(userId, channel, resource);
        var result = alertChannelSettingCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AlertChannelSettingResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
