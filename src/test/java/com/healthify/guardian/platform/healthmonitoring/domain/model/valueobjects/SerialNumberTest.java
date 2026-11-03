package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerialNumberTest {

    @Test
    void trimsWhitespace() {
        assertThat(new SerialNumber("  GP-ESP32-0001 ").value()).isEqualTo("GP-ESP32-0001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlank(String serialNumber) {
        assertThatThrownBy(() -> new SerialNumber(serialNumber))
                .hasMessage("wearable-device.serial-number.invalid");
    }
}
