package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.controllers;

import com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices.SafeZoneCommandServiceImpl;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.ProcessTelemetryCommand;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.acl.MobilityGeofencingContextFacadeImpl;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.LocationPoint;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.TelemetryIngestionResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/location-tracking")
public class LocationTrackingController {

    private final SafeZoneCommandServiceImpl safeZoneCommandService;
    private final MobilityGeofencingContextFacadeImpl mobilityFacade;

    public LocationTrackingController(SafeZoneCommandServiceImpl safeZoneCommandService,
                                      MobilityGeofencingContextFacadeImpl mobilityFacade) {
        this.safeZoneCommandService = safeZoneCommandService;
        this.mobilityFacade = mobilityFacade;
    }

    @PostMapping("/telemetry")
    public ResponseEntity<Map<String, String>> processTelemetry(@RequestBody TelemetryIngestionResource resource) {
        // 1. Crear el comando de procesamiento
        ProcessTelemetryCommand command = new ProcessTelemetryCommand(
                resource.careRecipientProfileId(),
                resource.latitude(),
                resource.longitude(),
                resource.accuracy() != null ? resource.accuracy() : 0.0f
        );

        // 2. Evaluar geocercas
        safeZoneCommandService.handle(command);

        // 3. Actualizar la última posición en la fachada ACL
        mobilityFacade.updateLastKnownLocation(
                resource.careRecipientProfileId(),
                new LocationPoint(resource.latitude(), resource.longitude(), resource.accuracy())
        );

        return ResponseEntity.ok(Map.of("message", "Lectura de telemetría procesada exitosamente."));
    }
}
