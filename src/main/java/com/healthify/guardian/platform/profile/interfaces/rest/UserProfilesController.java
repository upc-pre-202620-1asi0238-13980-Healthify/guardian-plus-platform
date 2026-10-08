package com.healthify.guardian.platform.profile.interfaces.rest;

import com.healthify.guardian.platform.profile.application.commandservices.UserProfileCommandService;
import com.healthify.guardian.platform.profile.application.queryservices.UserProfileQueryService;
import com.healthify.guardian.platform.profile.domain.model.queries.GetUserProfileByUserIdQuery;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.CreateUserProfileResource;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateContactInformationResource;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateUserProfileResource;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UserProfileResource;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.CreateUserProfileCommandFromResourceAssembler;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.UpdateContactInformationCommandFromResourceAssembler;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.UpdateUserProfileCommandFromResourceAssembler;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.UserProfileResourceFromEntityAssembler;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateProfileImageResource;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.UpdateUserProfileImageCommandFromResourceAssembler;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for Guardian+ user profiles.
 */
@RestController
@RequestMapping(
        value = "/api/v1/user-profiles",
        produces = APPLICATION_JSON_VALUE)
@Tag(
        name = "User Profiles",
        description = "User profile and contact information endpoints")
public class UserProfilesController {

    private final UserProfileCommandService userProfileCommandService;
    private final UserProfileQueryService userProfileQueryService;

    public UserProfilesController(
            UserProfileCommandService userProfileCommandService,
            UserProfileQueryService userProfileQueryService) {

        this.userProfileCommandService = userProfileCommandService;
        this.userProfileQueryService = userProfileQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Create a user profile",
            description = "Creates the descriptive profile associated with a Guardian+ user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User profile created",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserProfileResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"),
            @ApiResponse(
                    responseCode = "409",
                    description = "A profile already exists for the user")
    })
    public ResponseEntity<?> createUserProfile(
            @Valid @RequestBody CreateUserProfileResource resource) {

        var command =
                CreateUserProfileCommandFromResourceAssembler
                        .toCommandFromResource(resource);

        var result = userProfileCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserProfileResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get a user profile",
            description = "Retrieves the profile associated with a Guardian+ user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserProfileResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found")
    })
    public ResponseEntity<?> getUserProfileByUserId(
            @PathVariable
            @Parameter(
                    description = "Guardian+ user unique identifier",
                    required = true)
            UUID userId) {

        var profile = userProfileQueryService.handle(
                new GetUserProfileByUserIdQuery(
                        new UserId(userId)));

        if (profile.isEmpty()) {
            return ErrorResponseAssembler
                    .toErrorResponseFromApplicationError(
                            ApplicationError.notFound(
                                    "UserProfile",
                                    userId.toString()));
        }

        return ResponseEntity.ok(
                UserProfileResourceFromEntityAssembler
                        .toResourceFromEntity(profile.get()));
    }

    @PutMapping("/{userProfileId}")
    @Operation(
            summary = "Update personal information",
            description = "Updates the first and last name of a user profile.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile updated",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserProfileResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found")
    })
    public ResponseEntity<?> updateUserProfile(
            @PathVariable
            @Parameter(
                    description = "User profile unique identifier",
                    required = true)
            UUID userProfileId,

            @Valid
            @RequestBody
            UpdateUserProfileResource resource) {

        var command =
                UpdateUserProfileCommandFromResourceAssembler
                        .toCommandFromResource(
                                userProfileId,
                                resource);

        var result = userProfileCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserProfileResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @PutMapping("/{userProfileId}/contact-information")
    @Operation(
            summary = "Update contact information",
            description = "Updates the phone number of a user profile.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contact information updated",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserProfileResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found")
    })
    public ResponseEntity<?> updateContactInformation(
            @PathVariable
            @Parameter(
                    description = "User profile unique identifier",
                    required = true)
            UUID userProfileId,

            @Valid
            @RequestBody
            UpdateContactInformationResource resource) {

        var command =
                UpdateContactInformationCommandFromResourceAssembler
                        .toCommandFromResource(
                                userProfileId,
                                resource);

        var result = userProfileCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserProfileResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
    @PutMapping("/{userProfileId}/profile-image")
    @Operation(
            summary = "Update user profile image",
            description = "Updates the profile image associated with a Guardian+ user profile.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile image updated",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserProfileResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found")
    })
    public ResponseEntity<?> updateUserProfileImage(
            @PathVariable
            @Parameter(
                    description = "User profile unique identifier",
                    required = true)
            UUID userProfileId,

            @RequestBody
            UpdateProfileImageResource resource) {

        var command =
                UpdateUserProfileImageCommandFromResourceAssembler
                        .toCommandFromResource(
                                userProfileId,
                                resource);

        var result =
                userProfileCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserProfileResourceFromEntityAssembler
                        ::toResourceFromEntity,
                HttpStatus.OK);
    }
}