package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.controllers;

import com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices.CommunicationCommandService;
import com.healthify.guardian.platform.mobilitygeofencing.application.internal.queryservices.CommunicationQueryService;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallType;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.*;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.transform.CommunicationResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/communication")
@Tag(name = "Direct Communication", description = "US-23: Direct communication channel (video/voice calls, device capability evaluation, haptic alerts, and call logs)")
public class CommunicationController {

    private final CommunicationCommandService communicationCommandService;
    private final CommunicationQueryService communicationQueryService;

    public CommunicationController(
            CommunicationCommandService communicationCommandService,
            CommunicationQueryService communicationQueryService
    ) {
        this.communicationCommandService = communicationCommandService;
        this.communicationQueryService = communicationQueryService;
    }

    @GetMapping("/{fragileCitizenId}/device-capabilities")
    @Operation(
            summary = "Get wristband device capabilities",
            description = "Retrieves the hardware capabilities of the linked wearable (Guardian Pro, Guardian Band Lite, Guardian Core) and fallback phone numbers."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Device capabilities retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DeviceCapabilitiesResource.class))
            )
    })
    public ResponseEntity<DeviceCapabilitiesResource> getDeviceCapabilities(
            @PathVariable
            @Parameter(description = "Fragile citizen unique identifier", required = true)
            UUID fragileCitizenId
    ) {
        var citizenId = new FragileCitizenId(fragileCitizenId);
        var profile = communicationQueryService.getDeviceCapabilities(citizenId);
        return ResponseEntity.ok(
                CommunicationResourceAssembler.toDeviceCapabilitiesResource(citizenId, profile)
        );
    }

    @PostMapping("/calls/start")
    @Operation(
            summary = "Start a direct communication call",
            description = "Starts a VIDEO or VOICE call session with the fragile citizen's wearable, applying automatic degradation to VOICE if the model lacks a camera, or returning UNSUPPORTED if only telemetry/haptics are supported."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Call session evaluated and processed",
                    content = @Content(schema = @Schema(implementation = CallSessionResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<CallSessionResource> startCall(
            @RequestBody StartCallRequestResource request
    ) {
        if (request == null || request.fragileCitizenId() == null) {
            return ResponseEntity.badRequest().build();
        }
        CallType requestedType = parseCallType(request.requestedType());
        var citizenId = new FragileCitizenId(request.fragileCitizenId());
        var session = communicationCommandService.startCall(citizenId, requestedType);
        return ResponseEntity.ok(CommunicationResourceAssembler.toCallSessionResource(session));
    }

    @PostMapping("/calls/{callId}/end")
    @Operation(
            summary = "End an active call session",
            description = "Ends an active call session, calculates its duration, and logs the completed call in the call log history."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Call session ended successfully",
                    content = @Content(schema = @Schema(implementation = CallSessionResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Call session not found")
    })
    public ResponseEntity<CallSessionResource> endCall(
            @PathVariable
            @Parameter(description = "Call session unique identifier", required = true)
            UUID callId
    ) {
        return communicationCommandService.endCall(callId)
                .map(CommunicationResourceAssembler::toCallSessionResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/calls/log-unanswered")
    @Operation(
            summary = "Log an unanswered call after 30s timeout",
            description = "Registers an unanswered call attempt in the persistent call log and returns the updated call log list."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Unanswered call logged and updated history returned"
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<List<CallLogEntryResource>> registerUnansweredCall(
            @RequestBody RegisterCallLogRequestResource request
    ) {
        if (request == null || request.fragileCitizenId() == null) {
            return ResponseEntity.badRequest().build();
        }
        CallType attemptedType = parseCallType(request.attemptedType());
        var citizenId = new FragileCitizenId(request.fragileCitizenId());
        var updatedLogs = communicationCommandService.registerUnansweredCall(
                citizenId,
                attemptedType,
                request.reason()
        );
        var resources = updatedLogs.stream()
                .map(CommunicationResourceAssembler::toCallLogEntryResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{fragileCitizenId}/call-logs")
    @Operation(
            summary = "Get call log history of a fragile citizen",
            description = "Retrieves the ordered call log history (answered, degraded, unanswered, and unsupported attempts) for the specified fragile citizen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Call log history retrieved successfully"
            )
    })
    public ResponseEntity<List<CallLogEntryResource>> getCallLogs(
            @PathVariable
            @Parameter(description = "Fragile citizen unique identifier", required = true)
            UUID fragileCitizenId
    ) {
        var citizenId = new FragileCitizenId(fragileCitizenId);
        var logs = communicationQueryService.getCallLogs(citizenId);
        var resources = logs.stream()
                .map(CommunicationResourceAssembler::toCallLogEntryResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/haptic-alert")
    @Operation(
            summary = "Send a haptic vibration alert to the wearable",
            description = "Dispatches a vibratory alert (buzz) command to the fragile citizen's wristband."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Haptic alert sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<Map<String, String>> sendHapticAlert(
            @RequestBody HapticAlertRequestResource request
    ) {
        if (request == null || request.fragileCitizenId() == null) {
            return ResponseEntity.badRequest().build();
        }
        var citizenId = new FragileCitizenId(request.fragileCitizenId());
        communicationCommandService.sendHapticAlert(citizenId, request.message());
        return ResponseEntity.ok(Map.of(
                "status", "SENT",
                "fragileCitizenId", request.fragileCitizenId().toString(),
                "message", (request.message() != null && !request.message().isBlank())
                        ? request.message()
                        : "Alerta vibratoria enviada exitosamente al wearable."
        ));
    }

    private CallType parseCallType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return CallType.VOICE;
        }
        try {
            return CallType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CallType.VOICE;
        }
    }
}
