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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/safe-zones")
public class SafeZonesController {

    private final SafeZoneCommandService safeZoneCommandService;

    public SafeZonesController(SafeZoneCommandService safeZoneCommandService) {
        this.safeZoneCommandService = safeZoneCommandService;
    }

    @PostMapping
    public ResponseEntity<String> createSafeZone(@RequestBody CreateSafeZoneResource resource) {
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
    public ResponseEntity<Void> updateSafeZone(
            @PathVariable UUID safeZoneId,
            @RequestBody UpdateSafeZoneResource resource) {

        UpdateSafeZoneCommand command = new UpdateSafeZoneCommand(
                safeZoneId,
                resource.name(),
                new Coordinates(resource.centerLatitude(), resource.centerLongitude()),
                resource.radiusInMeters()
        );
        safeZoneCommandService.handle(command);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{safeZoneId}/activate")
    public ResponseEntity<Void> activateSafeZone(@PathVariable UUID safeZoneId) {
        ActivateSafeZoneCommand command = new ActivateSafeZoneCommand(safeZoneId);
        safeZoneCommandService.handle(command);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{safeZoneId}/deactivate")
    public ResponseEntity<Void> deactivateSafeZone(@PathVariable UUID safeZoneId) {
        DeactivateSafeZoneCommand command = new DeactivateSafeZoneCommand(safeZoneId);
        safeZoneCommandService.handle(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/fragile-citizen/{fragileCitizenId}/active")
    public ResponseEntity<SafeZoneResource> getActiveSafeZone(@PathVariable UUID fragileCitizenId) {
        return safeZoneCommandService.getActiveByFragileCitizenId(new FragileCitizenId(fragileCitizenId))
                .map(SafeZoneResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
