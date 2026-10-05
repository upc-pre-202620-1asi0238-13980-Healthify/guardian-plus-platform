package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.WearableDeviceCommandService;
import com.healthify.guardian.platform.healthmonitoring.application.queryservices.WearableDeviceQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetWearableDevicesByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LinkWearableDeviceResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.WearableDeviceResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.LinkWearableDeviceCommandFromResourceAssembler;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.WearableDeviceResourceFromEntityAssembler;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the wearable devices linked to care recipients.
 */
@RestController
@RequestMapping(value = "/api/v1/wearable-devices", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Wearable Devices", description = "Wearable device linking endpoints")
public class WearableDevicesController {

    private final WearableDeviceCommandService wearableDeviceCommandService;
    private final WearableDeviceQueryService wearableDeviceQueryService;

    public WearableDevicesController(WearableDeviceCommandService wearableDeviceCommandService,
                                     WearableDeviceQueryService wearableDeviceQueryService) {
        this.wearableDeviceCommandService = wearableDeviceCommandService;
        this.wearableDeviceQueryService = wearableDeviceQueryService;
    }

    @PostMapping
    @Operation(summary = "Link a wearable device", description = "Links a wearable device to a care recipient so its readings are accepted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device linked",
                    content = @Content(schema = @Schema(implementation = WearableDeviceResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "409", description = "Serial number already registered", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> linkWearableDevice(@Valid @RequestBody LinkWearableDeviceResource resource) {
        var result = wearableDeviceCommandService.handle(
                LinkWearableDeviceCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, WearableDeviceResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping("/care-recipient/{careRecipientProfileId}")
    @Operation(summary = "List wearable devices", description = "Lists the devices linked to a care recipient.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devices retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = WearableDeviceResource.class))))
    })
    public ResponseEntity<List<WearableDeviceResource>> getWearableDevices(
            @PathVariable @Parameter(description = "Care recipient profile unique identifier", required = true)
            UUID careRecipientProfileId) {
        var query = new GetWearableDevicesByCareRecipientProfileIdQuery(new CareRecipientProfileId(careRecipientProfileId));
        return ResponseEntity.ok(wearableDeviceQueryService.handle(query).stream()
                .map(WearableDeviceResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
