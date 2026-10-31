package com.healthify.guardian.platform.iam.interfaces.rest;

import com.healthify.guardian.platform.iam.application.queryservices.UserAccountQueryService;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.queries.GetUserAccountByIdQuery;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests of {@code /api/v1/user-accounts}: the signed-in account is the subject of the session token.
 */
class UserAccountsControllerTest {

    private final UserAccountQueryService userAccountQueryService = mock(UserAccountQueryService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserAccountsController(userAccountQueryService))
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static void signInAs(String subject) {
        var jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject(subject)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void meReturnsTheAccountOfTheToken() throws Exception {
        var account = new UserAccount(new RegisterUserCredentialsCommand("ana@guardian.pe", "secret-123"), new HashedPassword("hash"));
        when(userAccountQueryService.handle(any(GetUserAccountByIdQuery.class))).thenReturn(Optional.of(account));
        signInAs(account.getId().value().toString());

        mockMvc.perform(get("/api/v1/user-accounts/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId().value().toString()))
                .andExpect(jsonPath("$.email").value("ana@guardian.pe"));
    }

    @Test
    void meOfADeletedAccountIsNotFound() throws Exception {
        when(userAccountQueryService.handle(any(GetUserAccountByIdQuery.class))).thenReturn(Optional.empty());
        signInAs("6b0b1a3e-5f5e-4d6e-9a59-3c1c1f6c2b10");

        mockMvc.perform(get("/api/v1/user-accounts/me")).andExpect(status().isNotFound());
    }
}
