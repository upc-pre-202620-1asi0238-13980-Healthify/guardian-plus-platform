package com.healthify.guardian.platform.iam.interfaces.rest;

import com.healthify.guardian.platform.iam.application.queryservices.UserAccountQueryService;
import com.healthify.guardian.platform.iam.domain.model.queries.GetEmailAvailabilityQuery;
import com.healthify.guardian.platform.iam.domain.model.queries.GetUserAccountByIdQuery;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.EmailAvailabilityResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.UserAccountResource;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.UserAccountResourceFromEntityAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.ErrorResource;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for reading user accounts and checking whether an e-mail is free to register.
 */
@RestController
@RequestMapping(value = "/api/v1/user-accounts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "User Accounts", description = "User account endpoints")
public class UserAccountsController {

    private final UserAccountQueryService userAccountQueryService;

    public UserAccountsController(UserAccountQueryService userAccountQueryService) {
        this.userAccountQueryService = userAccountQueryService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the signed-in account",
            description = "Retrieves the account the session token (JWT) belongs to. Requires the token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account retrieved",
                    content = @Content(schema = @Schema(implementation = UserAccountResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing, invalid or expired token", content = @Content),
            @ApiResponse(responseCode = "404", description = "The account of the token no longer exists", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getCurrentUserAccount(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        // The security filter chain only lets this endpoint through with a valid token, whose subject is the account id
        var userAccountId = UUID.fromString(jwt.getSubject());
        return getUserAccount(userAccountId);
    }

    @GetMapping("/{userAccountId}")
    @Operation(summary = "Get a user account", description = "Retrieves an account without any credential data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account retrieved",
                    content = @Content(schema = @Schema(implementation = UserAccountResource.class))),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getUserAccount(
            @PathVariable @Parameter(description = "User account unique identifier", required = true) UUID userAccountId) {
        return userAccountQueryService.handle(new GetUserAccountByIdQuery(new UserAccountId(userAccountId)))
                .<ResponseEntity<?>>map(account -> ResponseEntity.ok(UserAccountResourceFromEntityAssembler.toResourceFromEntity(account)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("UserAccount", userAccountId.toString())));
    }

    @GetMapping("/availability")
    @Operation(summary = "Check e-mail availability", description = "Tells whether an e-mail is still free to register.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability retrieved",
                    content = @Content(schema = @Schema(implementation = EmailAvailabilityResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid e-mail", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<EmailAvailabilityResource> getEmailAvailability(
            @RequestParam @Parameter(description = "E-mail to check", required = true, example = "ana.torres@gmail.com") String email) {
        var available = userAccountQueryService.handle(new GetEmailAvailabilityQuery(email));
        return ResponseEntity.ok(new EmailAvailabilityResource(new Email(email).value(), available));
    }
}
