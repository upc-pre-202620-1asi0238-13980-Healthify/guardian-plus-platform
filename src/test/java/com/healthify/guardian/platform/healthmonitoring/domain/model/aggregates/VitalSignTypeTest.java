package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.ReadingClassification;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSignTypeTest {

    private static RegisterVitalSignTypeCommand command(String code, String name, String unit,
                                                        String normalMinimum, String normalMaximum,
                                                        String physicalMinimum, String physicalMaximum) {
        return new RegisterVitalSignTypeCommand(code, name, unit,
                new BigDecimal(normalMinimum), new BigDecimal(normalMaximum),
                new BigDecimal(physicalMinimum), new BigDecimal(physicalMaximum));
    }

    private static RegisterVitalSignTypeCommand heartRate() {
        return command("HR", "Heart rate", "bpm", "60", "100", "20", "250");
    }

    private static VitalSignValue value(String value) {
        return new VitalSignValue(new BigDecimal(value));
    }

    @Test
    void registersNormalizedCatalogEntry() {
        var type = new VitalSignType(command(" spo2 ", " Oxygen saturation ", "%", "92", "100", "0", "100"));

        assertThat(type.getCode().value()).isEqualTo("SPO2");
        assertThat(type.getName()).isEqualTo("Oxygen saturation");
        assertThat(type.getUnit()).isEqualTo("%");
        assertThat(type.getNormalRange()).isEqualTo(VitalSignRange.of("92", "100"));
        assertThat(type.getPhysicalLimits()).isEqualTo(VitalSignRange.of("0", "100"));
    }

    @Test
    void rejectsBlankNameOrUnit() {
        assertThatThrownBy(() -> new VitalSignType(command("HR", " ", "bpm", "60", "100", "20", "250")))
                .hasMessage("vital-sign-type.name.invalid");
        assertThatThrownBy(() -> new VitalSignType(new RegisterVitalSignTypeCommand("HR", "Heart rate", null,
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN)))
                .hasMessage("vital-sign-type.unit.invalid");
    }

    @Test
    void requiresBothReferenceRanges() {
        assertThatThrownBy(() -> new VitalSignType(new RegisterVitalSignTypeCommand("HR", "Heart rate", "bpm",
                null, new BigDecimal("100"), new BigDecimal("20"), new BigDecimal("250"))))
                .hasMessage("vital-sign-range.invalid");
        assertThatThrownBy(() -> new VitalSignType(command("HR", "Heart rate", "bpm", "60", "100", "250", "20")))
                .hasMessage("vital-sign-range.invalid");
    }

    @Test
    void rejectsNormalRangeBeyondPhysicalLimits() {
        assertThatThrownBy(() -> new VitalSignType(command("SPO2", "Oxygen saturation", "%", "92", "101", "0", "100")))
                .hasMessage("vital-sign-type.normal-range.outside-physical-limits");
    }

    @Test
    void classifiesReadingsAgainstItsNormalRange() {
        var type = new VitalSignType(heartRate());

        assertThat(type.classify(value("55"))).isEqualTo(ReadingClassification.BELOW_RANGE);
        assertThat(type.classify(value("60"))).isEqualTo(ReadingClassification.WITHIN_RANGE);
        assertThat(type.classify(value("100"))).isEqualTo(ReadingClassification.WITHIN_RANGE);
        assertThat(type.classify(value("101"))).isEqualTo(ReadingClassification.ABOVE_RANGE);
    }

    @Test
    void tellsWhetherAReadingIsPhysicallyPossible() {
        var type = new VitalSignType(heartRate());

        assertThat(type.isPhysicallyPossible(value("20"))).isTrue();
        assertThat(type.isPhysicallyPossible(value("250"))).isTrue();
        assertThat(type.isPhysicallyPossible(value("19"))).isFalse();
        assertThat(type.isPhysicallyPossible(value("400"))).isFalse();
    }

    @Test
    void allowsOnlyClinicalRangesWithinPhysicalLimits() {
        var type = new VitalSignType(heartRate());

        assertThat(type.allows(VitalSignRange.of("50", "120"))).isTrue();
        assertThat(type.allows(VitalSignRange.of("10", "120"))).isFalse();
    }

    @Test
    void typesWithoutReferenceRangesAcceptAnyReading() {
        var legacy = new VitalSignType();

        assertThat(legacy.hasReferenceRanges()).isFalse();
        assertThat(legacy.isPhysicallyPossible(value("-5"))).isTrue();
        assertThat(legacy.allows(VitalSignRange.of("0", "1000"))).isTrue();
    }

    @Test
    void completesReferenceRangesOfALegacyType() {
        var legacy = new VitalSignType();

        legacy.defineReferenceRanges(VitalSignRange.of("60", "100"), VitalSignRange.of("20", "250"));

        assertThat(legacy.hasReferenceRanges()).isTrue();
    }
}
