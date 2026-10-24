package com.healthify.guardian.platform.profile.interfaces.rest;

import com.healthify.guardian.platform.profile.application.commandservices.UserPreferencesCommandService;
import com.healthify.guardian.platform.profile.application.queryservices.UserPreferencesQueryService;
import com.healthify.guardian.platform.profile.domain.model.queries.GetUserPreferencesQuery;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateApplicationPreferencesResource;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateLanguageAndAccessibilityPreferencesResource;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UserPreferencesResource;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.UpdateApplicationPreferencesCommandFromResourceAssembler;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.UpdateLanguageAndAccessibilityPreferencesCommandFromResourceAssembler;
import com.healthify.guardian.platform.profile.interfaces.rest.transform.UserPreferencesResourceFromEntityAssembler;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for user application preferences.
 */
@RestController
@RequestMapping(
        value = "/api/v1/user-preferences",
        produces = APPLICATION_JSON_VALUE)
@Tag(
        name = "User Preferences",
        description = "Application, language and accessibility preferences")
public class UserPreferencesController {

    private final UserPreferencesCommandService
            userPreferencesCommandService;

    private final UserPreferencesQueryService
            userPreferencesQueryService;

    public UserPreferencesController(
            UserPreferencesCommandService userPreferencesCommandService,
            UserPreferencesQueryService userPreferencesQueryService) {

        this.userPreferencesCommandService =
                userPreferencesCommandService;

        this.userPreferencesQueryService =
                userPreferencesQueryService;
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user preferences",
            description = "Retrieves the application, language and accessibility preferences of a Guardian+ user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User preferences retrieved",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            UserPreferencesResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User preferences not found")
    })
    public ResponseEntity<?> getUserPreferences(
            @PathVariable
            @Parameter(
                    description = "Guardian+ user unique identifier",
                    required = true)
            UUID userId) {

        var preferences =
                userPreferencesQueryService.handle(
                        new GetUserPreferencesQuery(
                                new UserId(userId)));

        if (preferences.isEmpty()) {
            return ErrorResponseAssembler
                    .toErrorResponseFromApplicationError(
                            ApplicationError.notFound(
                                    "UserPreferences",
                                    userId.toString()));
        }

        return ResponseEntity.ok(
                UserPreferencesResourceFromEntityAssembler
                        .toResourceFromEntity(
                                preferences.get()));
    }

    @PutMapping("/user/{userId}/application")
    @Operation(
            summary = "Update application preferences",
            description = "Updates general application preferences such as notifications. Creates the preference set if it does not exist yet.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Application preferences updated",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            UserPreferencesResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data")
    })
    public ResponseEntity<?> updateApplicationPreferences(
            @PathVariable
            @Parameter(
                    description = "Guardian+ user unique identifier",
                    required = true)
            UUID userId,

            @Valid
            @RequestBody
            UpdateApplicationPreferencesResource resource) {

        var command =
                UpdateApplicationPreferencesCommandFromResourceAssembler
                        .toCommandFromResource(
                                userId,
                                resource);

        var result =
                userPreferencesCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserPreferencesResourceFromEntityAssembler
                        ::toResourceFromEntity,
                HttpStatus.OK);
    }

    @PutMapping("/user/{userId}/language-accessibility")
    @Operation(
            summary = "Update language and accessibility preferences",
            description = "Updates language, text size, high contrast and reduced motion preferences.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Language and accessibility preferences updated",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            UserPreferencesResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User preferences not found")
    })
    public ResponseEntity<?> updateLanguageAndAccessibilityPreferences(
            @PathVariable
            @Parameter(
                    description = "Guardian+ user unique identifier",
                    required = true)
            UUID userId,

            @Valid
            @RequestBody
            UpdateLanguageAndAccessibilityPreferencesResource resource) {

        var command =
                UpdateLanguageAndAccessibilityPreferencesCommandFromResourceAssembler
                        .toCommandFromResource(
                                userId,
                                resource);

        var result =
                userPreferencesCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserPreferencesResourceFromEntityAssembler
                        ::toResourceFromEntity,
                HttpStatus.OK);
    }
}