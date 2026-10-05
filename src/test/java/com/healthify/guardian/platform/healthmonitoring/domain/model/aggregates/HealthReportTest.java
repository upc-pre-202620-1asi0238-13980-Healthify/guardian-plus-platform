package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.GenerateHealthReportCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.entities.VitalSignSummary;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.HealthReportGeneratedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.WeeklySummaryCompiledEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HealthReportTest {

    private static final UUID RECIPIENT = UUID.randomUUID();
    private static final UUID HEART_RATE = UUID.randomUUID();
    private static final UUID SPO2 = UUID.randomUUID();
    private static final LocalDate START = LocalDate.of(2026, 9, 28);
    private static final LocalDate END = LocalDate.of(2026, 10, 4);

    private static VitalSign reading(UUID recipient, UUID type, String value) {
        var at = Instant.parse("2026-10-01T10:00:00Z");
        return new VitalSign(new DetectVitalSignsCommand(UUID.randomUUID(), recipient, type, new BigDecimal(value), at, at));
    }

    private static GenerateHealthReportCommand command(String type) {
        return new GenerateHealthReportCommand(RECIPIENT, UUID.randomUUID(), type, START, END);
    }

    private static String label(VitalSignTypeId typeId) {
        return typeId.value().equals(HEART_RATE) ? "HR" : "SPO2";
    }

    private static VitalSignThreshold threshold(UUID type, String min, String max) {
        return new VitalSignThreshold(new DefineVitalSignThresholdCommand(RECIPIENT, type, new BigDecimal(min), new BigDecimal(max), 3));
    }

    @Test
    void summarizesEachVitalSignType() {
        var readings = List.of(reading(RECIPIENT, HEART_RATE, "60"), reading(RECIPIENT, HEART_RATE, "80"),
                reading(RECIPIENT, SPO2, "97"));

        var report = new HealthReport(command("ON_DEMAND"), readings, List.of(), HealthReportTest::label);

        assertThat(report.getReportType()).isEqualTo(HealthReportType.ON_DEMAND);
        assertThat(report.getSummaries()).hasSize(2);
        var heartRate = report.getSummaries().getFirst();
        assertThat(heartRate.getMetricType()).isEqualTo("HR");
        assertThat(heartRate.getAverageValue()).isEqualTo(70.0);
        assertThat(heartRate.getMinValue()).isEqualTo(60.0);
        assertThat(heartRate.getMaxValue()).isEqualTo(80.0);
        assertThat(heartRate.getReadingsCount()).isEqualTo(2);
        assertThat(report.isClinicallyStable()).isTrue();
        assertThat(report.domainEvents()).singleElement().isInstanceOf(HealthReportGeneratedEvent.class);
    }

    @Test
    void flagsTypesWithMoreThanThreeAnomaliesAsRecurrent() {
        var heartRateReadings = Stream.of("120", "125", "130", "118", "70").map(v -> reading(RECIPIENT, HEART_RATE, v));
        var spo2Readings = Stream.of("89", "97").map(v -> reading(RECIPIENT, SPO2, v));
        var readings = Stream.concat(heartRateReadings, spo2Readings).toList();
        var thresholds = List.of(threshold(HEART_RATE, "60", "100"), threshold(SPO2, "95", "100"));

        var report = new HealthReport(command("ON_DEMAND"), readings, thresholds, HealthReportTest::label);

        assertThat(report.getSummaries()).extracting(VitalSignSummary::getStabilityIndex)
                .containsExactly(VitalSignSummary.RECURRENT, VitalSignSummary.UNSTABLE);
        assertThat(report.getSummaries().getFirst().getOutOfRangeCount()).isEqualTo(4);
        assertThat(report.getRecurrentAnomaliesCount()).isEqualTo(1);
        assertThat(report.isClinicallyStable()).isFalse();
    }

    @Test
    void weeklyReportAlsoAnnouncesTheWeeklySummary() {
        var report = new HealthReport(new GenerateHealthReportCommand(RECIPIENT, null, "WEEKLY_AUTOMATIC", START, END),
                List.of(reading(RECIPIENT, HEART_RATE, "70")), List.of(), HealthReportTest::label);

        assertThat(report.getGeneratedByUserId()).isNull();
        assertThat(report.domainEvents()).hasSize(2)
                .anySatisfy(event -> assertThat(event).isInstanceOf(WeeklySummaryCompiledEvent.class));
    }

    @Test
    void rejectsEmptyPeriod() {
        assertThatThrownBy(() -> new HealthReport(command("ON_DEMAND"), List.of(), List.of(), HealthReportTest::label))
                .hasMessage("health-report.vital-signs.empty");
    }

    @Test
    void rejectsReadingsOfAnotherCareRecipient() {
        var readings = List.of(reading(UUID.randomUUID(), HEART_RATE, "70"));

        assertThatThrownBy(() -> new HealthReport(command("ON_DEMAND"), readings, List.of(), HealthReportTest::label))
                .hasMessage("health-report.vital-sign.out-of-scope");
    }

    @Test
    void rejectsUnknownReportType() {
        var readings = List.of(reading(RECIPIENT, HEART_RATE, "70"));

        assertThatThrownBy(() -> new HealthReport(command("MONTHLY"), readings, List.of(), HealthReportTest::label))
                .hasMessage("health-report.report-type.invalid");
    }
}
