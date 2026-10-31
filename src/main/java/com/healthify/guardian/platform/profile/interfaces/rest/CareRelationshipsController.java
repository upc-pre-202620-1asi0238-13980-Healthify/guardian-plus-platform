package com.healthify.guardian.platform.profile.interfaces.rest;

import com.healthify.guardian.platform.profile.application.commandservices.CareRelationshipCommandService;
import com.healthify.guardian.platform.profile.application.queryservices.CareRelationshipQueryService;
import com.healthify.guardian.platform.profile.domain.model.commands.EndCareRelationshipCommand;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRelationshipsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRelationshipsByUserIdQuery;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.CareRelationshipResource;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.EstablishCareRelationshipResource;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.CareRelationshipResourceFromEntityAssembler;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.EstablishCareRelationshipCommandFromResourceAssembler;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for care relationships.
 */
@RestController
@RequestMapping(
        value = "/api/v1/care-relationships",
        produces = APPLICATION_JSON_VALUE)
@Tag(
        name = "Care Relationships",
        description = "Relationships between Guardian+ users and people under care")
public class CareRelationshipsController {

    private final CareRelationshipCommandService careRelationshipCommandService;
    private final CareRelationshipQueryService careRelationshipQueryService;

    public CareRelationshipsController(
            CareRelationshipCommandService careRelationshipCommandService,
            CareRelationshipQueryService careRelationshipQueryService) {

        this.careRelationshipCommandService =
                careRelationshipCommandService;

        this.careRelationshipQueryService =
                careRelationshipQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Establish a care relationship",
            description = "Creates an active relationship between a Guardian+ user and a person under care.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Care relationship established",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CareRelationshipResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Care recipient profile not found"),
            @ApiResponse(
                    responseCode = "409",
                    description = "An active relationship already exists")
    })
    public ResponseEntity<?> establishCareRelationship(
            @Valid
            @RequestBody
            EstablishCareRelationshipResource resource) {

        var command =
                EstablishCareRelationshipCommandFromResourceAssembler
                        .toCommandFromResource(resource);

        var result =
                careRelationshipCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CareRelationshipResourceFromEntityAssembler
                        ::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "List active relationships by user",
            description = "Retrieves the active care relationships of a Guardian+ user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active care relationships retrieved")
    })
    public ResponseEntity<List<CareRelationshipResource>>
    getCareRelationshipsByUserId(
            @PathVariable
            @Parameter(
                    description = "Guardian+ user unique identifier",
                    required = true)
            UUID userId) {

        var query =
                new GetCareRelationshipsByUserIdQuery(
                        new UserId(userId));

        var resources =
                careRelationshipQueryService.handle(query)
                        .stream()
                        .map(CareRelationshipResourceFromEntityAssembler
                                ::toResourceFromEntity)
                        .toList();

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/care-recipient/{careRecipientProfileId}")
    @Operation(
            summary = "List active relationships by care recipient",
            description = "Retrieves the active relationships of a person under care.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active care relationships retrieved")
    })
    public ResponseEntity<List<CareRelationshipResource>>
    getCareRelationshipsByCareRecipientProfileId(
            @PathVariable
            @Parameter(
                    description = "Care recipient profile unique identifier",
                    required = true)
            UUID careRecipientProfileId) {

        var query =
                new GetCareRelationshipsByCareRecipientProfileIdQuery(
                        new CareRecipientProfileId(
                                careRecipientProfileId));

        var resources =
                careRelationshipQueryService.handle(query)
                        .stream()
                        .map(CareRelationshipResourceFromEntityAssembler
                                ::toResourceFromEntity)
                        .toList();

        return ResponseEntity.ok(resources);
    }

    @DeleteMapping("/{careRelationshipId}")
    @Operation(
            summary = "End a care relationship",
            description = "Ends an active care relationship while preserving its history.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Care relationship ended",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CareRelationshipResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Care relationship not found"),
            @ApiResponse(
                    responseCode = "422",
                    description = "Care relationship is already ended")
    })
    public ResponseEntity<?> endCareRelationship(
            @PathVariable
            @Parameter(
                    description = "Care relationship unique identifier",
                    required = true)
            UUID careRelationshipId) {

        var result =
                careRelationshipCommandService.handle(
                        new EndCareRelationshipCommand(
                                careRelationshipId));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CareRelationshipResourceFromEntityAssembler
                        ::toResourceFromEntity,
                HttpStatus.OK);
    }
}