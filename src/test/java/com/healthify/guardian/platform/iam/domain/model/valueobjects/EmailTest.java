package com.healthify.guardian.platform.iam.domain.model.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void normalizesCaseAndSurroundingSpaces() {
        assertThat(new Email("  Ana.Torres@Guardian.PE ").value()).isEqualTo("ana.torres@guardian.pe");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "ana", "ana@", "@guardian.pe", "ana@guardian", "ana..torres@guardian.pe", "ana torres@guardian.pe"})
    void rejectsInvalidAddresses(String value) {
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user-account.email.invalid");
    }

    @Test
    void matchesTheSameAddressIgnoringCase() {
        var email = new Email("ana@guardian.pe");

        assertThat(email.matches(" ANA@guardian.pe")).isTrue();
        assertThat(email.matches("otra@guardian.pe")).isFalse();
        assertThat(email.matches(null)).isFalse();
    }
}
