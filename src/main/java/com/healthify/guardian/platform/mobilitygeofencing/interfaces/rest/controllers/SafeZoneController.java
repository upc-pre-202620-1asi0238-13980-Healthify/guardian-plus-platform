package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.controllers;

import com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices.SafeZoneCommandServiceImpl;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.CreateSafeZoneCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.CreateSafeZoneResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/safe-zones")
public class SafeZoneController {

    private final SafeZoneCommandServiceImpl safeZoneCommandService;

    public SafeZoneController(SafeZoneCommandServiceImpl safeZoneCommandService) {
        this.safeZoneCommandService = safeZoneCommandService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSafeZone(@RequestBody CreateSafeZoneResource resource) {
        CreateSafeZoneCommand command = new CreateSafeZoneCommand(
                resource.careRecipientProfileId(),
                resource.name(),
                resource.latitude(),
                resource.longitude(),
                resource.radiusInMeters()
        );

        SafeZoneId createdId = safeZoneCommandService.handle(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", createdId.value(),
                        "message", "Safe zone successfully created"
                ));
    }
}
