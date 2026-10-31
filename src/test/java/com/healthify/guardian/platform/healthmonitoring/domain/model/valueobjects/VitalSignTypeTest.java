package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSignTypeTest {

    private static VitalSignValue value(String value) {
        return new VitalSignValue(new BigDecimal(value));
    }

    @ParameterizedTest
    @EnumSource(VitalSignType.class)
    void everyNormalRangeLiesWithinThePhysicalLimitsOfItsType(VitalSignType type) {
        assertThat(type.physicalLimits().encloses(type.normalRange())).isTrue();
        assertThat(type.code()).isEqualTo(type.name());
        assertThat(type.unit()).isNotBlank();
    }

    @Test
    void resolvesTypesFromTheirCodeIgnoringCaseAndBlanks() {
        assertThat(VitalSignType.fromCode(" spo2 ")).isEqualTo(VitalSignType.SPO2);
        assertThat(VitalSignType.fromCode("RESP_RATE")).isEqualTo(VitalSignType.RESP_RATE);
    }

    @Test
    void rejectsUnknownOrBlankCodes() {
        assertThatThrownBy(() -> VitalSignType.fromCode("GLUCOSE")).hasMessage("vital-sign.type.invalid");
        assertThatThrownBy(() -> VitalSignType.fromCode(" ")).hasMessage("vital-sign.type.invalid");
        assertThatThrownBy(() -> VitalSignType.fromCode(null)).hasMessage("vital-sign.type.invalid");
    }

    @ParameterizedTest
    @CsvSource({
            "HR,59,BELOW_RANGE", "HR,60,WITHIN_RANGE", "HR,100,WITHIN_RANGE", "HR,101,ABOVE_RANGE",
            "BP_SYS,150,ABOVE_RANGE", "BP_DIA,55,BELOW_RANGE",
            "SPO2,91,BELOW_RANGE", "SPO2,92,WITHIN_RANGE",
            "TEMP,35.9,BELOW_RANGE", "TEMP,37.5,WITHIN_RANGE", "TEMP,37.6,ABOVE_RANGE",
            "RESP_RATE,16,WITHIN_RANGE", "RESP_RATE,21,ABOVE_RANGE"})
    void classifiesReadingsAgainstTheNormalRangeOfEachType(VitalSignType type, String reading,
                                                          ReadingClassification expected) {
        assertThat(type.classify(value(reading))).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"HR,20,true", "HR,250,true", "HR,19,false", "HR,400,false",
            "SPO2,0,true", "SPO2,101,false", "TEMP,43.0,true", "TEMP,43.1,false"})
    void tellsWhetherAReadingIsPhysicallyPossible(VitalSignType type, String reading, boolean possible) {
        assertThat(type.isPhysicallyPossible(value(reading))).isEqualTo(possible);
    }
}
