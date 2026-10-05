package com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.interfaces.events.VitalSignAnomalyDetectedIntegrationEvent;
import com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext.NOW;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of the Tolerance Rule through the whole Detect -> Emit -> Evaluate chain.
 */
class VitalSignsThresholdsEvaluatedEventHandlerTest {

    private final HealthMonitoringTestContext context = new HealthMonitoringTestContext();
    private final UUID recipient = UUID.randomUUID();
    private VitalSignType heartRate;
    private WearableDevice device;
    private int secondsAgo = 600;

    @BeforeEach
    void setUp() {
        heartRate = context.registerType("HR", "Heart rate", "bpm");
        device = context.assignDevice(recipient, "GP-0001");
        context.defineThreshold(recipient, heartRate, "60", "100", 3);
    }

    private void read(String... values) {
        for (var value : values) {
            context.detect(device, heartRate, value, NOW.minusSeconds(secondsAgo--));
        }
    }

    private int anomalies() {
        return context.eventsOfType(VitalSignAnomalyDetectedIntegrationEvent.class).size();
    }

    @Test
    void everyDetectedReadingIsEmittedAndEvaluated() {
        read("72");

        var stored = context.vitalSignRepository.findAll().getFirst();
        assertThat(stored.isEmitted()).isTrue();
        assertThat(anomalies()).isZero();
    }

    @Test
    void twoConsecutiveDeviationsAreTolerated() {
        read("120", "125");

        assertThat(anomalies()).isZero();
    }

    @Test
    void thirdConsecutiveDeviationConfirmsTheAnomalyOnce() {
        read("120", "125", "130", "135");

        assertThat(anomalies()).isEqualTo(1);
        var anomaly = context.eventsOfType(VitalSignAnomalyDetectedIntegrationEvent.class).getFirst();
        assertThat(anomaly.vitalSignTypeCode()).isEqualTo("HR");
        assertThat(anomaly.classification()).isEqualTo("ABOVE_RANGE");
        assertThat(anomaly.value()).isEqualByComparingTo("130");
        assertThat(anomaly.careRecipientProfileId()).isEqualTo(recipient);
    }

    @Test
    void aNormalReadingBreaksTheStreak() {
        read("120", "125", "80", "130", "135");

        assertThat(anomalies()).isZero();
    }

    @Test
    void aNewStreakAfterRecoveryRaisesANewAnomaly() {
        read("120", "125", "130", "80", "40", "45", "42");

        assertThat(anomalies()).isEqualTo(2);
        assertThat(context.eventsOfType(VitalSignAnomalyDetectedIntegrationEvent.class).getLast().classification())
                .isEqualTo("BELOW_RANGE");
    }

    @Test
    void readingsWithoutThresholdAreNeverAnomalous() {
        var spo2 = context.registerType("SPO2", "Oxygen saturation", "%");
        for (int i = 0; i < 4; i++) {
            context.detect(device, spo2, "70", NOW.minusSeconds(100 - i));
        }

        assertThat(anomalies()).isZero();
    }
}
