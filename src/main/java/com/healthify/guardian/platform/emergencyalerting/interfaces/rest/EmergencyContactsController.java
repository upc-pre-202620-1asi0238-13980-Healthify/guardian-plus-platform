package com.healthify.guardian.platform.emergencyalerting.interfaces.rest;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.EmergencyContactCommandService;
import com.healthify.guardian.platform.emergencyalerting.application.queryservices.EmergencyContactQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DeactivateEmergencyContactCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ReorderEmergencyContactsCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetEmergencyContactsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AddEmergencyContactResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.EmergencyContactResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.ReorderEmergencyContactsResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.AddEmergencyContactCommandFromResourceAssembler;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.EmergencyContactResourceFromEntityAssembler;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the emergency contacts of a person under care.
 */
@RestController
@RequestMapping(value = "/api/v1/emergency-contacts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Emergency Contacts", description = "Escalation chain of the Care Circle members who receive alerts")
public class EmergencyContactsController {

    private final EmergencyContactCommandService emergencyContactCommandService;
    private final EmergencyContactQueryService emergencyContactQueryService;

    public EmergencyContactsController(
            EmergencyContactCommandService emergencyContactCommandService,
            EmergencyContactQueryService emergencyContactQueryService) {
        this.emergencyContactCommandService = emergencyContactCommandService;
        this.emergencyContactQueryService = emergencyContactQueryService;
    }

    @GetMapping("/care-recipient/{careRecipientProfileId}")
    @Operation(
            summary = "List the emergency contacts of a person under care",
            description = "Retrieves the active emergency contacts, primary contact (priority 1) first."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emergency contacts retrieved successfully")
    })
    public ResponseEntity<List<EmergencyContactResource>> getEmergencyContacts(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID careRecipientProfileId
    ) {
        var query = new GetEmergencyContactsByCareRecipientProfileIdQuery(
                new CareRecipientProfileId(careRecipientProfileId));
        return ResponseEntity.ok(emergencyContactQueryService.handle(query).stream()
                .map(EmergencyContactResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @PostMapping
    @Operation(
            summary = "Add an emergency contact",
            description = "Registers a Care Circle member as an emergency contact at the requested priority (last if " +
                    "omitted), shifting the following contacts down. A previously removed contact is reactivated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Emergency contact added",
                    content = @Content(schema = @Schema(implementation = EmergencyContactResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "The user is already an active emergency contact"),
            @ApiResponse(responseCode = "422", description = "The user has no active care relationship with the person")
    })
    public ResponseEntity<?> addEmergencyContact(@Valid @RequestBody AddEmergencyContactResource resource) {
        var result = emergencyContactCommandService.handle(
                AddEmergencyContactCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, EmergencyContactResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PutMapping("/care-recipient/{careRecipientProfileId}/order")
    @Operation(
            summary = "Reorder the emergency contacts",
            description = "Reassigns the priority of every active contact following the given order; the first one " +
                    "becomes the primary contact."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emergency contacts reordered"),
            @ApiResponse(responseCode = "400", description = "The order does not list every active contact exactly once")
    })
    public ResponseEntity<?> reorderEmergencyContacts(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID careRecipientProfileId,
            @Valid @RequestBody ReorderEmergencyContactsResource resource
    ) {
        var result = emergencyContactCommandService.handle(
                new ReorderEmergencyContactsCommand(careRecipientProfileId, resource.orderedEmergencyContactIds()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                contacts -> contacts.stream()
                        .map(EmergencyContactResourceFromEntityAssembler::toResourceFromEntity)
                        .toList(),
                HttpStatus.OK);
    }

    @DeleteMapping("/{emergencyContactId}")
    @Operation(
            summary = "Remove an emergency contact",
            description = "Deactivates the contact so it receives no further alerts. The person under care must keep " +
                    "at least one active contact."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Emergency contact removed",
                    content = @Content(schema = @Schema(implementation = EmergencyContactResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Emergency contact not found"),
            @ApiResponse(responseCode = "422", description = "It is the last active contact or already removed")
    })
    public ResponseEntity<?> removeEmergencyContact(
            @PathVariable
            @Parameter(description = "Emergency contact unique identifier", required = true)
            UUID emergencyContactId
    ) {
        var result = emergencyContactCommandService.handle(new DeactivateEmergencyContactCommand(emergencyContactId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, EmergencyContactResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
