package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSignRangeTest {

    @Test
    void rejectsMissingOrInvertedBounds() {
        assertThatThrownBy(() -> new VitalSignRange(null, BigDecimal.TEN)).hasMessage("vital-sign-range.invalid");
        assertThatThrownBy(() -> new VitalSignRange(BigDecimal.ONE, null)).hasMessage("vital-sign-range.invalid");
        assertThatThrownBy(() -> VitalSignRange.of("10", "1")).hasMessage("vital-sign-range.invalid");
    }

    @Test
    void acceptsASingleValueRange() {
        assertThat(VitalSignRange.of("100", "100").contains(new VitalSignValue(new BigDecimal("100")))).isTrue();
    }

    @Test
    void classifiesWithInclusiveBounds() {
        var range = VitalSignRange.of("36.0", "37.5");

        assertThat(range.classify(new VitalSignValue(new BigDecimal("35.9")))).isEqualTo(ReadingClassification.BELOW_RANGE);
        assertThat(range.classify(new VitalSignValue(new BigDecimal("36.0")))).isEqualTo(ReadingClassification.WITHIN_RANGE);
        assertThat(range.classify(new VitalSignValue(new BigDecimal("37.50")))).isEqualTo(ReadingClassification.WITHIN_RANGE);
        assertThat(range.classify(new VitalSignValue(new BigDecimal("37.6")))).isEqualTo(ReadingClassification.ABOVE_RANGE);
    }

    @Test
    void enclosesOnlyRangesWithinItsBounds() {
        var limits = VitalSignRange.of("0", "100");

        assertThat(limits.encloses(VitalSignRange.of("92", "100"))).isTrue();
        assertThat(limits.encloses(VitalSignRange.of("92", "101"))).isFalse();
        assertThat(limits.encloses(null)).isFalse();
    }
}
