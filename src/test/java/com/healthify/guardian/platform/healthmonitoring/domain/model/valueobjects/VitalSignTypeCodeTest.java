package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSignTypeCodeTest {

    @Test
    void normalizesToTrimmedUpperCase() {
        assertThat(new VitalSignTypeCode("  spo2 ").value()).isEqualTo("SPO2");
    }

    @Test
    void codesThatDifferOnlyInCaseAreEqual() {
        assertThat(new VitalSignTypeCode("hr")).isEqualTo(new VitalSignTypeCode("HR"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlank(String code) {
        assertThatThrownBy(() -> new VitalSignTypeCode(code)).hasMessage("vital-sign-type.code.invalid");
    }
}
