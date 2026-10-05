package com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
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
    private WearableDevice device;
    private int secondsAgo = 600;

    @BeforeEach
    void setUp() {
        device = context.linkDevice(recipient, "GP-0001");
    }

    private void read(String... values) {
        for (var value : values) {
            context.detect(device, VitalSignType.HR, value, NOW.minusSeconds(secondsAgo--));
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
        assertThat(anomaly.minimumValue()).isEqualByComparingTo("60");
        assertThat(anomaly.maximumValue()).isEqualByComparingTo("100");
        assertThat(anomaly.consecutiveReadings()).isEqualTo(3);
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
    void eachVitalSignTypeIsEvaluatedAgainstItsOwnNormalRange() {
        for (int i = 0; i < 3; i++) {
            context.detect(device, VitalSignType.SPO2, "85", NOW.minusSeconds(100 - i));
        }

        assertThat(anomalies()).isEqualTo(1);
        var anomaly = context.eventsOfType(VitalSignAnomalyDetectedIntegrationEvent.class).getLast();
        assertThat(anomaly.vitalSignTypeCode()).isEqualTo("SPO2");
        assertThat(anomaly.classification()).isEqualTo("BELOW_RANGE");
        assertThat(anomaly.minimumValue()).isEqualByComparingTo("92");
    }

    @Test
    void streaksOfTheSameCareRecipientAndTypeShareTheThresholdReference() {
        read("120", "125", "130", "80", "40", "45", "42");

        var anomalies = context.eventsOfType(VitalSignAnomalyDetectedIntegrationEvent.class);
        assertThat(anomalies.getFirst().vitalSignThresholdId()).isEqualTo(anomalies.getLast().vitalSignThresholdId());
        assertThat(anomalies.getFirst().vitalSignTypeId()).isNotNull();
    }
}
