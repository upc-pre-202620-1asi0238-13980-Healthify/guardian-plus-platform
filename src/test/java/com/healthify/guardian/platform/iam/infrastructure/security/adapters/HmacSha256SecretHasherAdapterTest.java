package com.healthify.guardian.platform.iam.infrastructure.security.adapters;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class HmacSha256SecretHasherAdapterTest {

    private static final String KEY = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());

    @Test
    void theSameSecretAlwaysGivesTheSameHash() {
        var hasher = new HmacSha256SecretHasherAdapter(KEY);

        assertThat(hasher.hash("048213")).isEqualTo(hasher.hash("048213"));
        assertThat(hasher.hash("048213").hash()).hasSize(64).doesNotContain("048213");
    }

    @Test
    void matchesOnlyTheOriginalSecret() {
        var hasher = new HmacSha256SecretHasherAdapter(KEY);
        var hash = hasher.hash("048213");

        assertThat(hasher.matches("048213", hash)).isTrue();
        assertThat(hasher.matches("048214", hash)).isFalse();
        assertThat(hasher.matches(null, hash)).isFalse();
    }

    @Test
    void aDifferentKeyGivesADifferentHash() {
        var other = Base64.getEncoder().encodeToString("fedcba9876543210fedcba9876543210".getBytes());

        assertThat(new HmacSha256SecretHasherAdapter(KEY).hash("048213"))
                .isNotEqualTo(new HmacSha256SecretHasherAdapter(other).hash("048213"));
    }

    @Test
    void worksWithARandomKeyWhenNoneIsConfigured() {
        var hasher = new HmacSha256SecretHasherAdapter("");

        assertThat(hasher.matches("048213", hasher.hash("048213"))).isTrue();
        assertThat(hasher.matches("048213", new HashedSecret("not-the-hash"))).isFalse();
    }
}
