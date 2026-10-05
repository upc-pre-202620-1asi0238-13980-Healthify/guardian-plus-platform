package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.ReadingClassification;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSignThresholdTest {

    private static VitalSignThreshold define(String min, String max, Integer hits) {
        return new VitalSignThreshold(new DefineVitalSignThresholdCommand(UUID.randomUUID(), UUID.randomUUID(),
                min == null ? null : new BigDecimal(min), max == null ? null : new BigDecimal(max), hits));
    }

    private static VitalSignValue value(String value) {
        return new VitalSignValue(new BigDecimal(value));
    }

    @Test
    void isDefinedActive() {
        var threshold = define("90", "100", 3);

        assertThat(threshold.isActive()).isTrue();
        assertThat(threshold.getRequiredConsecutiveHits()).isEqualTo(3);
    }

    @Test
    void boundsAreInclusive() {
        var spo2 = define("95", "100", 3);

        assertThat(spo2.classify(value("95"))).isEqualTo(ReadingClassification.WITHIN_RANGE);
        assertThat(spo2.classify(value("100"))).isEqualTo(ReadingClassification.WITHIN_RANGE);
        assertThat(spo2.classify(value("89.9"))).isEqualTo(ReadingClassification.BELOW_RANGE);
        assertThat(spo2.isExceededBy(value("100.1"))).isTrue();
    }

    @Test
    void rejectsMinimumAboveMaximum() {
        assertThatThrownBy(() -> define("101", "100", 3)).hasMessage("vital-sign-threshold.range.invalid");
        assertThatThrownBy(() -> define(null, "100", 3)).hasMessage("vital-sign-threshold.range.invalid");
    }

    @Test
    void rejectsLessThanOneConsecutiveReading() {
        assertThatThrownBy(() -> define("60", "100", 0)).hasMessage("vital-sign-threshold.consecutive-hits.invalid");
    }

    @Test
    void redefineChangesTheRangeAndReactivates() {
        var threshold = define("60", "100", 3);
        threshold.deactivate();

        threshold.redefine(new BigDecimal("50"), new BigDecimal("110"), 2);

        assertThat(threshold.isActive()).isTrue();
        assertThat(threshold.getMinimumValue()).isEqualByComparingTo("50");
        assertThat(threshold.getRequiredConsecutiveHits()).isEqualTo(2);
    }

    @Test
    void activationTransitionsAreGuarded() {
        var threshold = define("60", "100", 3);

        assertThatThrownBy(threshold::activate).hasMessage("vital-sign-threshold.already-active");
        threshold.deactivate();
        assertThat(threshold.isActive()).isFalse();
        assertThatThrownBy(threshold::deactivate).hasMessage("vital-sign-threshold.already-inactive");
    }
}
