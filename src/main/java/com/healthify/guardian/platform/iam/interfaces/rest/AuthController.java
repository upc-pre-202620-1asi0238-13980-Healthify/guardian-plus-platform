package com.healthify.guardian.platform.iam.interfaces.rest;

import com.healthify.guardian.platform.iam.application.commandservices.AuthenticationCommandService;
import com.healthify.guardian.platform.iam.application.commandservices.OneTimePasswordCommandService;
import com.healthify.guardian.platform.iam.application.commandservices.UserAccountCommandService;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.AuthenticatedSessionResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.LoginResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.OtpChallengeResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.RegisterUserCredentialsResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.RequestPasswordResetResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.ResendLoginOtpResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.ResetPasswordResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.SendEmailVerificationRequestResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.UserAccountResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.VerifyEmailResource;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.VerifyOtpResource;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.AuthenticatedSessionResourceFromTokenAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.LoginCommandFromResourceAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.RegisterUserCredentialsCommandFromResourceAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.RequestPasswordResetCommandFromResourceAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.ResendLoginOtpCommandFromResourceAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.ResetPasswordCommandFromResourceAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.SendEmailVerificationRequestCommandFromResourceAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.UserAccountResourceFromEntityAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.VerifyEmailCommandFromResourceAssembler;
import com.healthify.guardian.platform.iam.interfaces.rest.transform.VerifyOtpCommandFromResourceAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.ErrorResource;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.MessageResource;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for registration, e-mail verification, the two-step login (password, then OTP) and password reset.
 * All its endpoints are public: they are how a user gets a session token in the first place.
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Registration, login with OTP and password reset endpoints")
@SecurityRequirements
public class AuthController {

    private final UserAccountCommandService userAccountCommandService;
    private final OneTimePasswordCommandService oneTimePasswordCommandService;
    private final AuthenticationCommandService authenticationCommandService;

    public AuthController(UserAccountCommandService userAccountCommandService,
                          OneTimePasswordCommandService oneTimePasswordCommandService,
                          AuthenticationCommandService authenticationCommandService) {
        this.userAccountCommandService = userAccountCommandService;
        this.oneTimePasswordCommandService = oneTimePasswordCommandService;
        this.authenticationCommandService = authenticationCommandService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register user credentials",
            description = "Creates an account pending e-mail verification and e-mails it a verification code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Credentials registered",
                    content = @Content(schema = @Schema(implementation = UserAccountResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "409", description = "E-mail already registered", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserCredentialsResource resource) {
        var result = userAccountCommandService.handle(
                RegisterUserCredentialsCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, UserAccountResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify the e-mail", description = "Confirms the e-mail with the code sent to it and activates the account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail verified",
                    content = @Content(schema = @Schema(implementation = UserAccountResource.class))),
            @ApiResponse(responseCode = "400", description = "Wrong or expired code", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "E-mail already verified", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailResource resource) {
        var result = userAccountCommandService.handle(VerifyEmailCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, UserAccountResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping("/verify-email/resend")
    @Operation(summary = "Send a new e-mail verification code", description = "Sends a new code to an account still pending verification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Code sent", content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "E-mail already verified", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> resendEmailVerification(@Valid @RequestBody SendEmailVerificationRequestResource resource) {
        var result = oneTimePasswordCommandService.handle(
                SendEmailVerificationRequestCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, otp -> message("iam.email-verification.sent"), HttpStatus.ACCEPTED);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in (step 1 of 2)",
            description = "Checks the e-mail and password and e-mails the login code (OTP). Complete the login with POST /api/v1/auth/otp/verify.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Credentials correct, OTP sent",
                    content = @Content(schema = @Schema(implementation = OtpChallengeResource.class))),
            @ApiResponse(responseCode = "401", description = "Wrong e-mail or password", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Right password, but the e-mail is not verified yet; details carry the user account id",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginResource resource) {
        var result = authenticationCommandService.handle(LoginCommandFromResourceAssembler.toCommandFromResource(resource));
        return toResponseEntity(result,
                account -> new OtpChallengeResource(account.getId().value(), message("iam.otp.sent").message()),
                HttpStatus.ACCEPTED);
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify the login code (step 2 of 2)",
            description = "Checks the OTP sent by e-mail and, if it is correct, authenticates the user and returns the session token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated",
                    content = @Content(schema = @Schema(implementation = AuthenticatedSessionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Wrong, expired or used code", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpResource resource) {
        var result = authenticationCommandService.handle(VerifyOtpCommandFromResourceAssembler.toCommandFromResource(resource));
        return toResponseEntity(result,
                token -> AuthenticatedSessionResourceFromTokenAssembler.toResourceFromToken(resource.userAccountId(), token),
                HttpStatus.OK);
    }

    @PostMapping("/otp/resend")
    @Operation(summary = "Send a new login code",
            description = "E-mails a new login code (OTP) to an account that passed the password step in the last minutes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Code sent", content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "401", description = "No login in progress for the account", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Too many codes requested", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> resendLoginOtp(@Valid @RequestBody ResendLoginOtpResource resource) {
        var result = oneTimePasswordCommandService.handle(
                ResendLoginOtpCommandFromResourceAssembler.toCommandFromResource(resource));
        return toResponseEntity(result, otp -> message("iam.otp.sent"), HttpStatus.ACCEPTED);
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Request a password reset", description = "E-mails a password reset token to the account with that e-mail.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Token generated and sent", content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid e-mail", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "No account with that e-mail", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody RequestPasswordResetResource resource) {
        var result = userAccountCommandService.handle(
                RequestPasswordResetCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, account -> message("iam.password-reset.sent"), HttpStatus.ACCEPTED);
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Set a new password", description = "Sets a new password with the token received by e-mail; the token can be used once.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed", content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token, or invalid password", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        var result = userAccountCommandService.handle(ResetPasswordCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, account -> message("iam.password-reset.completed"), HttpStatus.OK);
    }

    /**
     * Like {@link ResponseEntityAssembler}, but answers a failed login or OTP check with {@code 401 Unauthorized} and
     * an unverified e-mail with {@code 403 Forbidden}, statuses the shared error assembler does not produce.
     */
    private static <T, R> ResponseEntity<?> toResponseEntity(Result<T, ApplicationError> result,
                                                            Function<T, R> successResourceAssembler,
                                                            HttpStatusCode successStatus) {
        if (result instanceof Result.Failure<T, ApplicationError> failure
                && AuthenticationCommandService.AUTHENTICATION_FAILED.equals(failure.error().code())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResource(failure.error().code(), failure.error().message()));
        }
        if (result instanceof Result.Failure<T, ApplicationError> failure
                && AuthenticationCommandService.EMAIL_NOT_VERIFIED.equals(failure.error().code())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResource(failure.error().code(), failure.error().message(), failure.error().details()));
        }
        return ResponseEntityAssembler.toResponseEntityFromResult(result, successResourceAssembler, successStatus);
    }

    private static MessageResource message(String key) {
        return new MessageResource(MessageResolver.resolveOrDefault(key, key));
    }
}
