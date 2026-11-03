package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsDetectedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsEmittedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsThresholdsEvaluatedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.ReadingClassification;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSignTest {

    private static final UUID DEVICE = UUID.randomUUID();
    private static final UUID RECIPIENT = UUID.randomUUID();
    private static final Instant MEASURED = Instant.parse("2026-10-05T14:59:58Z");
    private static final Instant RECEIVED = Instant.parse("2026-10-05T15:00:00Z");

    private static VitalSign detect(String value) {
        return new VitalSign(new DetectVitalSignsCommand(DEVICE, RECIPIENT, "HR", new BigDecimal(value), MEASURED, RECEIVED));
    }


    @Test
    void detectionCapturesTheReadingAndRegistersDetectedEvent() {
        var vitalSign = detect("72");

        assertThat(vitalSign.getId()).isNotNull();
        assertThat(vitalSign.getValue().value()).isEqualByComparingTo("72");
        assertThat(vitalSign.isEmitted()).isFalse();
        assertThat(vitalSign.domainEvents()).singleElement().isInstanceOf(VitalSignsDetectedEvent.class);
    }

    @Test
    void rejectsReadingMeasuredAfterItWasReceived() {
        assertThatThrownBy(() -> new VitalSign(new DetectVitalSignsCommand(
                DEVICE, RECIPIENT, "HR", new BigDecimal("72"), RECEIVED.plusSeconds(1), RECEIVED)))
                .hasMessage("vital-sign.measured-at.after-received-at");
    }

    @Test
    void rejectsMissingMeasurementTime() {
        assertThatThrownBy(() -> new VitalSign(new DetectVitalSignsCommand(
                DEVICE, RECIPIENT, "HR", new BigDecimal("72"), null, RECEIVED)))
                .hasMessage("vital-sign.measured-at.invalid");
    }

    @Test
    void emitPublishesTheReadingOnlyOnce() {
        var vitalSign = detect("72");
        vitalSign.clearDomainEvents();

        vitalSign.emit(RECEIVED);

        assertThat(vitalSign.getEmittedAt()).isEqualTo(RECEIVED);
        assertThat(vitalSign.domainEvents()).singleElement().isInstanceOf(VitalSignsEmittedEvent.class);
        assertThatThrownBy(() -> vitalSign.emit(RECEIVED)).hasMessage("vital-sign.already-emitted");
    }

    @Test
    void cannotBeEvaluatedBeforeBeingEmitted() {
        var vitalSign = detect("72");

        assertThatThrownBy(() -> vitalSign.evaluateThresholds(RECEIVED))
                .hasMessage("vital-sign.not-emitted");
    }

    @ParameterizedTest
    @CsvSource({"72,WITHIN_RANGE,false", "60,WITHIN_RANGE,false", "100,WITHIN_RANGE,false",
            "112,ABOVE_RANGE,true", "45,BELOW_RANGE,true"})
    void evaluationClassifiesTheReadingAgainstTheNormalRangeOfItsType(String value, ReadingClassification expected, boolean deviates) {
        var vitalSign = detect(value);
        vitalSign.emit(RECEIVED);
        vitalSign.clearDomainEvents();

        var hasDeviation = vitalSign.evaluateThresholds(RECEIVED);

        assertThat(hasDeviation).isEqualTo(deviates);
        assertThat(vitalSign.domainEvents()).singleElement()
                .isInstanceOfSatisfying(VitalSignsThresholdsEvaluatedEvent.class, event -> {
                    assertThat(event.vitalSignType()).isEqualTo(VitalSignType.HR);
                    assertThat(event.classification()).isEqualTo(expected);
                    assertThat(event.hasDeviation()).isEqualTo(deviates);
                });
    }

    @Test
    void detectionResolvesTheVitalSignTypeFromItsCode() {
        var vitalSign = new VitalSign(new DetectVitalSignsCommand(
                DEVICE, RECIPIENT, " spo2 ", new BigDecimal("97"), MEASURED, RECEIVED));

        assertThat(vitalSign.getVitalSignType()).isEqualTo(VitalSignType.SPO2);
    }

    @Test
    void rejectsUnknownVitalSignType() {
        assertThatThrownBy(() -> new VitalSign(new DetectVitalSignsCommand(
                DEVICE, RECIPIENT, "GLUCOSE", new BigDecimal("90"), MEASURED, RECEIVED)))
                .hasMessage("vital-sign.type.invalid");
    }

    @ParameterizedTest
    @CsvSource({"HR,400", "HR,10", "SPO2,101", "TEMP,45.5", "RESP_RATE,0"})
    void rejectsReadingOutsideThePhysicalLimitsOfItsType(String type, String value) {
        assertThatThrownBy(() -> new VitalSign(new DetectVitalSignsCommand(
                DEVICE, RECIPIENT, type, new BigDecimal(value), MEASURED, RECEIVED)))
                .hasMessage("vital-sign.value.outside-physical-limits");
    }
}
