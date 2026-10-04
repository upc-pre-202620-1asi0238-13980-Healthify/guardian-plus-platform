package com.healthify.guardian.platform.profile.interfaces.rest;

import com.healthify.guardian.platform.profile.application.commandservices.CareRecipientProfileCommandService;
import com.healthify.guardian.platform.profile.application.queryservices.CareRecipientProfileQueryService;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRecipientProfileQuery;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.CareRecipientProfileResource;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.CreateCareRecipientProfileResource;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.CareRecipientProfileResourceFromEntityAssembler;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.CreateCareRecipientProfileCommandFromResourceAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for profiles of people under care.
 */
@RestController
@RequestMapping(
        value = "/api/v1/care-recipient-profiles",
        produces = APPLICATION_JSON_VALUE)
@Tag(
        name = "Care Recipient Profiles",
        description = "Profiles of people under care")
public class CareRecipientProfilesController {

    private final CareRecipientProfileCommandService
            careRecipientProfileCommandService;

    private final CareRecipientProfileQueryService
            careRecipientProfileQueryService;

    public CareRecipientProfilesController(
            CareRecipientProfileCommandService careRecipientProfileCommandService,
            CareRecipientProfileQueryService careRecipientProfileQueryService) {

        this.careRecipientProfileCommandService =
                careRecipientProfileCommandService;

        this.careRecipientProfileQueryService =
                careRecipientProfileQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Create a care recipient profile",
            description = "Creates the profile of a person under care.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Care recipient profile created",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            CareRecipientProfileResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data")
    })
    public ResponseEntity<?> createCareRecipientProfile(
            @Valid
            @RequestBody
            CreateCareRecipientProfileResource resource) {

        var command =
                CreateCareRecipientProfileCommandFromResourceAssembler
                        .toCommandFromResource(resource);

        var result =
                careRecipientProfileCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CareRecipientProfileResourceFromEntityAssembler
                        ::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    @GetMapping("/{careRecipientProfileId}")
    @Operation(
            summary = "Get a care recipient profile",
            description = "Retrieves a person under care profile by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Care recipient profile retrieved",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            CareRecipientProfileResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Care recipient profile not found")
    })
    public ResponseEntity<?> getCareRecipientProfile(
            @PathVariable
            @Parameter(
                    description = "Care recipient profile unique identifier",
                    required = true)
            UUID careRecipientProfileId) {

        var profile =
                careRecipientProfileQueryService.handle(
                        new GetCareRecipientProfileQuery(
                                new CareRecipientProfileId(
                                        careRecipientProfileId)));

        if (profile.isEmpty()) {
            return ErrorResponseAssembler
                    .toErrorResponseFromApplicationError(
                            ApplicationError.notFound(
                                    "CareRecipientProfile",
                                    careRecipientProfileId.toString()));
        }

        return ResponseEntity.ok(
                CareRecipientProfileResourceFromEntityAssembler
                        .toResourceFromEntity(profile.get()));
    }
}