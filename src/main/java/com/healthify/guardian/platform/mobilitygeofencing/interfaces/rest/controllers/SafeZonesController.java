package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.controllers;

import com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices.SafeZoneCommandService;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Coordinates;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.CreateSafeZoneResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.SafeZoneResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.UpdateSafeZoneResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.transform.SafeZoneResourceAssembler;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/safe-zones")
@Tag(name = "Safe Zones", description = "Managing safe zones, boundaries, activation state, and geolocation configuration for fragile citizens")
public class SafeZonesController {

    private final SafeZoneCommandService safeZoneCommandService;

    public SafeZonesController(SafeZoneCommandService safeZoneCommandService) {
        this.safeZoneCommandService = safeZoneCommandService;
    }


    @PostMapping
    @Operation(
            summary = "Create a safe zone",
            description = "Registers a new geofenced safe zone for a fragile citizen specifying center coordinates and a radius in meters."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Safe zone created successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data or parameters")
    })
    public ResponseEntity<String> createSafeZone(
            @Valid @RequestBody CreateSafeZoneResource resource) {
        CreateSafeZoneCommand command = new CreateSafeZoneCommand(
                resource.fragileCitizenId(),
                resource.name(),
                new Coordinates(resource.centerLatitude(), resource.centerLongitude()),
                resource.radiusInMeters()
        );

        SafeZoneId id = safeZoneCommandService.handle(command);
        return new ResponseEntity<>(id.value().toString(), HttpStatus.CREATED);
    }
    @PutMapping("/{safeZoneId}")
    @Operation(
            summary = "Update an existing safe zone",
            description = "Updates the configuration details of an existing safe zone, such as its name, center coordinates, or radius."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Safe zone updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Safe zone not found")
    })
    public ResponseEntity<Void> updateSafeZone(
            @PathVariable
            @Parameter(description = "Safe zone unique identifier", required = true)
            UUID safeZoneId,
            @Valid @RequestBody UpdateSafeZoneResource resource) {

        UpdateSafeZoneCommand command = new UpdateSafeZoneCommand(
                safeZoneId,
                resource.name(),
                new Coordinates(resource.centerLatitude(), resource.centerLongitude()),
                resource.radiusInMeters()
        );
        return found(safeZoneCommandService.handle(command), ResponseEntity.ok().build());
    }

    @PatchMapping("/{safeZoneId}/activate")
    @Operation(
            summary = "Activate a safe zone",
            description = "Enables monitoring for a specific safe zone so that boundary monitoring alerts are actively triggered."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Safe zone activated successfully"),
            @ApiResponse(responseCode = "404", description = "Safe zone not found")
    })
    public ResponseEntity<Void> activateSafeZone(
            @PathVariable @Parameter(description = "Safe zone unique identifier", required = true) UUID safeZoneId) {
        ActivateSafeZoneCommand command = new ActivateSafeZoneCommand(safeZoneId);
        return found(safeZoneCommandService.handle(command), ResponseEntity.ok().build());
    }


    @PatchMapping("/{safeZoneId}/deactivate")
    @Operation(
            summary = "Deactivate a safe zone",
            description = "Disables monitoring for a specific safe zone temporarily without deleting it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Safe zone deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Safe zone not found")
    })
    public ResponseEntity<Void> deactivateSafeZone(
            @PathVariable @Parameter(description = "Safe zone unique identifier", required = true)
            UUID safeZoneId) {
        DeactivateSafeZoneCommand command = new DeactivateSafeZoneCommand(safeZoneId);
        return found(safeZoneCommandService.handle(command), ResponseEntity.ok().build());
    }

    @GetMapping("/fragile-citizen/{fragileCitizenId}")
    @Operation(
            summary = "Get the safe zones of a fragile citizen",
            description = "Retrieves every safe zone configured for the specified fragile citizen, active or not, oldest first. At most one of them is active."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Safe zones retrieved successfully; empty when none is configured")
    })
    public ResponseEntity<List<SafeZoneResource>> getSafeZones(
            @PathVariable @Parameter(description = "Fragile citizen unique identifier", required = true)
            UUID fragileCitizenId) {
        var safeZones = safeZoneCommandService.getAllByFragileCitizenId(new FragileCitizenId(fragileCitizenId)).stream()
                .map(SafeZoneResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(safeZones);
    }

    @GetMapping("/fragile-citizen/{fragileCitizenId}/active")
    @Operation(
            summary = "Get active safe zone for a fragile citizen",
            description = "Retrieves the currently active safe zone configured for the specified fragile citizen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active safe zone retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SafeZoneResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Active safe zone or citizen not found")
    })
    public ResponseEntity<SafeZoneResource> getActiveSafeZone(
            @PathVariable @Parameter(description = "Fragile citizen unique identifier", required = true)
            UUID fragileCitizenId) {
        return safeZoneCommandService.getActiveByFragileCitizenId(new FragileCitizenId(fragileCitizenId))
                .map(SafeZoneResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{safeZoneId}")
    @Operation(
            summary = "Delete a safe zone",
            description = "Permanently removes a safe zone configuration from the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Safe zone deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Safe zone not found")
    })
    public ResponseEntity<Void> deleteSafeZone(
            @PathVariable @Parameter(description = "Safe zone unique identifier", required = true)
            UUID safeZoneId) {
        DeleteSafeZoneCommand command = new DeleteSafeZoneCommand(safeZoneId);
        return found(safeZoneCommandService.handle(command), ResponseEntity.noContent().build());
    }

    private static ResponseEntity<Void> found(boolean found, ResponseEntity<Void> response) {
        return found ? response : ResponseEntity.notFound().build();
    }
}
