package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSignValueTest {

    @Test
    void keepsAnyNumericReading() {
        assertThat(new VitalSignValue(new BigDecimal("72")).value()).isEqualByComparingTo("72");
        assertThat(new VitalSignValue(new BigDecimal("36.8")).value()).isEqualByComparingTo("36.8");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new VitalSignValue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("vital-sign.value.invalid");
    }
}
