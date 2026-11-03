package com.healthify.guardian.platform.iam.infrastructure.security.adapters;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureRandomSecretGeneratorAdapterTest {

    private final SecureRandomSecretGeneratorAdapter generator = new SecureRandomSecretGeneratorAdapter();

    @Test
    void numericCodesKeepTheirLeadingZeros() {
        IntStream.range(0, 1_000).forEach(i -> assertThat(generator.generateNumericCode(6)).matches("\\d{6}"));
    }

    @Test
    void rejectsAnInvalidNumberOfDigits() {
        assertThatThrownBy(() -> generator.generateNumericCode(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void opaqueTokensAreUrlSafeAndUnique() {
        var tokens = new HashSet<String>();
        IntStream.range(0, 100).forEach(i -> tokens.add(generator.generateOpaqueToken()));

        assertThat(tokens).hasSize(100).allMatch(token -> token.matches("[A-Za-z0-9_-]{43}"));
    }
}
