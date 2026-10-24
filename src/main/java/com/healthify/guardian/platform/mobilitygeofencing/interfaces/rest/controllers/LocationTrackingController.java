package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.controllers;

import com.healthify.guardian.platform.mobilitygeofencing.application.internal.queryservices.LocationTrackingQueryService;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.queries.GetLocationHistoryQuery;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.CurrentLocationResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.LocationHistoryResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.transform.LocationResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/location-tracking")
@Tag(name = "Location Tracking", description = "Tracking real-time locations, current status, and location history of fragile citizens")
public class LocationTrackingController {

    private final LocationTrackingQueryService locationTrackingQueryService;

    public LocationTrackingController(LocationTrackingQueryService locationTrackingQueryService) {
        this.locationTrackingQueryService = locationTrackingQueryService;
    }

    @GetMapping("/{fragileCitizenId}/current")
    @Operation(
            summary = "Get current location of a fragile citizen",
            description = "Retrieves the latest recorded geographical position and tracking details for the specified fragile citizen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current location found successfully",
                    content = @Content(schema = @Schema(implementation = CurrentLocationResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Tracking record or citizen not found")
    })
    public ResponseEntity<CurrentLocationResource> getCurrentLocation(
            @PathVariable
            @Parameter(description = "Fragile citizen unique identifier", required = true)
            UUID fragileCitizenId) {
        var fragileCitizen = new FragileCitizenId(fragileCitizenId);
        return locationTrackingQueryService.getCurrentTracking(fragileCitizen)
                .map(LocationResourceAssembler::toCurrentResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{fragileCitizenId}/status")
    @Operation(
            summary = "Get current status of a fragile citizen",
            description = "Retrieves the current mobility status (e.g., SAFE, OUT_OF_BOUNDS, UNKNOWN) based on the latest tracking information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "404", description = "Tracking record or citizen not found")
    })
    public ResponseEntity<String> getCurrentStatus(
            @PathVariable
            @Parameter(description = "Fragile citizen unique identifier", required = true)
            UUID fragileCitizenId) {
        var fragileCitizen = new FragileCitizenId(fragileCitizenId);
        return locationTrackingQueryService.getCurrentTracking(fragileCitizen)
                .map(tracking -> tracking.getCurrentStatus() != null ? tracking.getCurrentStatus().name() : "UNKNOWN")
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{fragileCitizenId}/history")
    @Operation(
            summary = "Get location history of a fragile citizen",
            description = "Retrieves a historical list of recorded locations for a fragile citizen within an optional ISO-8601 date-time range. If no dates are provided, it defaults to the last 24 hours."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Location history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LocationHistoryResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid date format or parameters")
    })
    public ResponseEntity<List<LocationHistoryResource>> getLocationHistory(
            @PathVariable
            @Parameter(description = "Fragile citizen unique identifier", required = true)
            UUID fragileCitizenId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant end) {

        Instant queryStart = (start != null) ? start : Instant.now().minusSeconds(86400);
        Instant queryEnd = (end != null) ? end : Instant.now();

        var query = new GetLocationHistoryQuery(new FragileCitizenId(fragileCitizenId), queryStart, queryEnd);
        var locations = locationTrackingQueryService.handle(query);

        var historyResources = locations.stream()
                .map(LocationResourceAssembler::toHistoryResource)
                .toList();

        return ResponseEntity.ok(historyResources);
    }
}