package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DeactivateVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sociable tests of {@link VitalSignThresholdCommandServiceImpl} over in-memory repositories.
 */
class VitalSignThresholdCommandServiceImplTest {

    private final HealthMonitoringTestContext context = new HealthMonitoringTestContext();
    private final UUID recipient = UUID.randomUUID();

    @Test
    void definingTwiceRedefinesTheSameThreshold() {
        var heartRate = context.registerType("HR", "Heart rate", "bpm");
        var first = context.defineThreshold(recipient, heartRate, "60", "100", 3);

        var second = context.defineThreshold(recipient, heartRate, "50", "110", 2);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getMaximumValue()).isEqualByComparingTo("110");
    }

    @Test
    void rejectsUnknownVitalSignType() {
        var result = context.thresholdCommandService.handle(new DefineVitalSignThresholdCommand(
                recipient, UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN, 3));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VITALSIGNTYPE_NOT_FOUND");
    }

    @Test
    void rejectsInvertedRange() {
        var heartRate = context.registerType("HR", "Heart rate", "bpm");

        var result = context.thresholdCommandService.handle(new DefineVitalSignThresholdCommand(
                recipient, heartRate.getId().value(), BigDecimal.TEN, BigDecimal.ONE, 3));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");
    }

    @Test
    void rejectsRangeBeyondThePhysicalLimitsOfTheType() {
        var spo2 = context.registerType("SPO2", "Oxygen saturation", "%");

        var result = context.thresholdCommandService.handle(new DefineVitalSignThresholdCommand(
                recipient, spo2.getId().value(), new BigDecimal("90"), new BigDecimal("110"), 3));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");
    }

    @Test
    void deactivatingTwiceViolatesABusinessRule() {
        var heartRate = context.registerType("HR", "Heart rate", "bpm");
        var threshold = context.defineThreshold(recipient, heartRate, "60", "100", 3);
        var command = new DeactivateVitalSignThresholdCommand(threshold.getId().value());

        assertThat(context.thresholdCommandService.handle(command).isSuccess()).isTrue();
        assertThat(((Result.Failure<?, ?>) context.thresholdCommandService.handle(command)).error())
                .hasFieldOrPropertyWithValue("code", "BUSINESS_RULE_VIOLATION");
    }
}
