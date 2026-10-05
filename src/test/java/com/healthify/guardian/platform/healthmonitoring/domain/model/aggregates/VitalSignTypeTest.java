package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSignTypeTest {

    @Test
    void registersNormalizedCatalogEntry() {
        var type = new VitalSignType(new RegisterVitalSignTypeCommand(" spo2 ", " Oxygen saturation ", "%"));

        assertThat(type.getCode().value()).isEqualTo("SPO2");
        assertThat(type.getName()).isEqualTo("Oxygen saturation");
        assertThat(type.getUnit()).isEqualTo("%");
    }

    @Test
    void rejectsBlankNameOrUnit() {
        assertThatThrownBy(() -> new VitalSignType(new RegisterVitalSignTypeCommand("HR", " ", "bpm")))
                .hasMessage("vital-sign-type.name.invalid");
        assertThatThrownBy(() -> new VitalSignType(new RegisterVitalSignTypeCommand("HR", "Heart rate", null)))
                .hasMessage("vital-sign-type.unit.invalid");
    }
}
