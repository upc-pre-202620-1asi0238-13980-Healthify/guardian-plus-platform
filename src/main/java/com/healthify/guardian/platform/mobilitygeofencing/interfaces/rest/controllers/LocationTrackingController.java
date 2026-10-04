package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.controllers;

import com.healthify.guardian.platform.mobilitygeofencing.application.internal.queryservices.LocationTrackingQueryService;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.queries.GetLocationHistoryQuery;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.CurrentLocationResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.LocationHistoryResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.transform.LocationResourceAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/location-tracking")
public class LocationTrackingController {

    private final LocationTrackingQueryService locationTrackingQueryService;

    public LocationTrackingController(LocationTrackingQueryService locationTrackingQueryService) {
        this.locationTrackingQueryService = locationTrackingQueryService;
    }

    @GetMapping("/{fragileCitizenId}/current")
    public ResponseEntity<CurrentLocationResource> getCurrentLocation(@PathVariable UUID fragileCitizenId) {
        var fragileCitizen = new FragileCitizenId(fragileCitizenId);
        return locationTrackingQueryService.getCurrentTracking(fragileCitizen)
                .map(LocationResourceAssembler::toCurrentResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{fragileCitizenId}/status")
    public ResponseEntity<String> getCurrentStatus(@PathVariable UUID fragileCitizenId) {
        var fragileCitizen = new FragileCitizenId(fragileCitizenId);
        return locationTrackingQueryService.getCurrentTracking(fragileCitizen)
                .map(tracking -> tracking.getCurrentStatus() != null ? tracking.getCurrentStatus().name() : "UNKNOWN")
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{fragileCitizenId}/history")
    public ResponseEntity<List<LocationHistoryResource>> getLocationHistory(
            @PathVariable UUID fragileCitizenId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

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