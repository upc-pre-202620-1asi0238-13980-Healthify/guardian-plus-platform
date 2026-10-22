package com.healthify.guardian.platform.iam.infrastructure.security.adapters;

import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.infrastructure.security.configuration.JwtConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class JwtTokenProviderAdapterTest {

    private static final UserAccount ACCOUNT = new UserAccount(
            new RegisterUserCredentialsCommand("ana@guardian.pe", "secret-123"), new HashedPassword("hash"));

    private static JwtTokenProviderAdapter provider(String issuer) {
        var configuration = new JwtConfiguration("", "", issuer);
        return new JwtTokenProviderAdapter(configuration.jwtEncoder(), configuration.jwtDecoder(), issuer, 60);
    }

    @Test
    void anIssuedTokenResolvesToItsAccount() {
        var provider = provider("guardian-plus-platform");

        var token = provider.issue(ACCOUNT);

        assertThat(token.expiresAt()).isCloseTo(Instant.now().plus(Duration.ofMinutes(60)), within(Duration.ofSeconds(5)));
        assertThat(provider.resolveUserAccountId(token.accessToken())).contains(ACCOUNT.getId());
    }

    @Test
    void rejectsATamperedToken() {
        var provider = provider("guardian-plus-platform");
        var token = provider.issue(ACCOUNT).accessToken();
        var tampered = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");

        assertThat(provider.resolveUserAccountId(tampered)).isEmpty();
        assertThat(provider.resolveUserAccountId("not-a-jwt")).isEmpty();
    }

    @Test
    void rejectsATokenSignedWithAnotherKey() {
        var token = provider("guardian-plus-platform").issue(ACCOUNT).accessToken();

        assertThat(provider("guardian-plus-platform").resolveUserAccountId(token)).isEmpty();
    }
}
