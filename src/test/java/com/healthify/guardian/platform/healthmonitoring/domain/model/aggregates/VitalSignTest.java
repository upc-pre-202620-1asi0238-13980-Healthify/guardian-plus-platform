package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsDetectedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsEmittedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsThresholdsEvaluatedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.ReadingClassification;
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
    private static final UUID HEART_RATE = UUID.randomUUID();
    private static final Instant MEASURED = Instant.parse("2026-10-05T14:59:58Z");
    private static final Instant RECEIVED = Instant.parse("2026-10-05T15:00:00Z");

    private static VitalSign detect(String value) {
        return new VitalSign(new DetectVitalSignsCommand(DEVICE, RECIPIENT, HEART_RATE, new BigDecimal(value), MEASURED, RECEIVED));
    }

    private static VitalSignThreshold heartRateThreshold(UUID recipient, UUID type) {
        return new VitalSignThreshold(new DefineVitalSignThresholdCommand(
                recipient, type, new BigDecimal("60"), new BigDecimal("100"), 3));
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
                DEVICE, RECIPIENT, HEART_RATE, BigDecimal.ONE, RECEIVED.plusSeconds(1), RECEIVED)))
                .hasMessage("vital-sign.measured-at.after-received-at");
    }

    @Test
    void rejectsMissingMeasurementTime() {
        assertThatThrownBy(() -> new VitalSign(new DetectVitalSignsCommand(
                DEVICE, RECIPIENT, HEART_RATE, BigDecimal.ONE, null, RECEIVED)))
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

        assertThatThrownBy(() -> vitalSign.evaluateThresholds(heartRateThreshold(RECIPIENT, HEART_RATE), RECEIVED))
                .hasMessage("vital-sign.not-emitted");
    }

    @ParameterizedTest
    @CsvSource({"72,WITHIN_RANGE,false", "60,WITHIN_RANGE,false", "100,WITHIN_RANGE,false",
            "112,ABOVE_RANGE,true", "45,BELOW_RANGE,true"})
    void evaluationClassifiesTheReadingAgainstTheThreshold(String value, ReadingClassification expected, boolean deviates) {
        var vitalSign = detect(value);
        vitalSign.emit(RECEIVED);
        vitalSign.clearDomainEvents();

        var hasDeviation = vitalSign.evaluateThresholds(heartRateThreshold(RECIPIENT, HEART_RATE), RECEIVED);

        assertThat(hasDeviation).isEqualTo(deviates);
        assertThat(vitalSign.domainEvents()).singleElement()
                .isInstanceOfSatisfying(VitalSignsThresholdsEvaluatedEvent.class, event -> {
                    assertThat(event.classification()).isEqualTo(expected);
                    assertThat(event.hasDeviation()).isEqualTo(deviates);
                });
    }

    @Test
    void rejectsThresholdOfAnotherCareRecipient() {
        var vitalSign = detect("72");
        vitalSign.emit(RECEIVED);

        assertThatThrownBy(() -> vitalSign.evaluateThresholds(heartRateThreshold(UUID.randomUUID(), HEART_RATE), RECEIVED))
                .hasMessage("vital-sign.threshold.mismatch");
    }
}
