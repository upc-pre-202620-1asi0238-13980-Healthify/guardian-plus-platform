package com.healthify.guardian.platform.iam.interfaces.rest;

import com.healthify.guardian.platform.iam.application.commandservices.AuthenticationCommandService;
import com.healthify.guardian.platform.iam.application.commandservices.OneTimePasswordCommandService;
import com.healthify.guardian.platform.iam.application.commandservices.UserAccountCommandService;
import com.healthify.guardian.platform.iam.application.outboundservices.SessionToken;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.LoginCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.VerifyOtpCommand;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests of the HTTP contract of {@code /api/v1/auth}: status codes and payloads, over mocked application services.
 */
class AuthControllerTest {

    private final UserAccountCommandService userAccountCommandService = mock(UserAccountCommandService.class);
    private final AuthenticationCommandService authenticationCommandService = mock(AuthenticationCommandService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userAccountCommandService,
                    mock(OneTimePasswordCommandService.class), authenticationCommandService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static UserAccount account() {
        return new UserAccount(new RegisterUserCredentialsCommand("ana@guardian.pe", "secret-123"), new HashedPassword("hash"));
    }

    private static String json(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }

    @Test
    void registeringReturnsTheAccountWithoutCredentials() throws Exception {
        when(userAccountCommandService.handle(any(RegisterUserCredentialsCommand.class))).thenReturn(Result.success(account()));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana@guardian.pe", "secret-123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@guardian.pe"))
                .andExpect(jsonPath("$.status").value("PENDING_EMAIL_VERIFICATION"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void rejectsAShortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana@guardian.pe", "short")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void theRightPasswordAsksForTheOtp() throws Exception {
        var account = account();
        when(authenticationCommandService.handle(any(LoginCommand.class)))
                .thenReturn(Result.success(account));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana@guardian.pe", "secret-123")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.userAccountId").value(account.getId().value().toString()))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void aFailedLoginIsUnauthorized() throws Exception {
        when(authenticationCommandService.handle(any(LoginCommand.class)))
                .thenReturn(Result.failure(new ApplicationError(AuthenticationCommandService.AUTHENTICATION_FAILED, "E-mail or password is incorrect")));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana@guardian.pe", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AuthenticationCommandService.AUTHENTICATION_FAILED));
    }

    @Test
    void theRightOtpReturnsTheSessionToken() throws Exception {
        var userAccountId = UUID.randomUUID();
        when(authenticationCommandService.handle(any(VerifyOtpCommand.class)))
                .thenReturn(Result.success(new SessionToken("jwt", Instant.parse("2026-10-22T20:00:00Z"))));

        mockMvc.perform(post("/api/v1/auth/otp/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccountId\":\"%s\",\"code\":\"048213\"}".formatted(userAccountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userAccountId").value(userAccountId.toString()));
    }

    @Test
    void anOtpMustHaveSixDigits() throws Exception {
        mockMvc.perform(post("/api/v1/auth/otp/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccountId\":\"%s\",\"code\":\"12ab\"}".formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnverifiedEmailIsForbiddenAndCarriesTheAccountId() throws Exception {
        var userAccountId = UUID.randomUUID().toString();
        when(authenticationCommandService.handle(any(LoginCommand.class))).thenReturn(Result.failure(new ApplicationError(
                AuthenticationCommandService.EMAIL_NOT_VERIFIED, "Verify your e-mail before logging in", userAccountId)));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana@guardian.pe", "secret-123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(AuthenticationCommandService.EMAIL_NOT_VERIFIED))
                .andExpect(jsonPath("$.details").value(userAccountId));
    }
}
