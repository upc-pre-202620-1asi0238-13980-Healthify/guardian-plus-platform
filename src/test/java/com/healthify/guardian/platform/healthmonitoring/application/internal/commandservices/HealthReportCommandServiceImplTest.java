package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.CompileWeeklySummaryCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.GenerateHealthReportCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.interfaces.events.HealthReportCompiledIntegrationEvent;
import com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext.NOW;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sociable tests of {@link HealthReportCommandServiceImpl} over in-memory repositories.
 */
class HealthReportCommandServiceImplTest {

    private final HealthMonitoringTestContext context = new HealthMonitoringTestContext();
    private final UUID recipient = UUID.randomUUID();

    @Test
    void emptyPeriodViolatesABusinessRule() {
        var result = context.healthReportCommandService.handle(new GenerateHealthReportCommand(
                recipient, UUID.randomUUID(), "ON_DEMAND", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "BUSINESS_RULE_VIOLATION");
    }

    @Test
    void invertedPeriodIsAValidationError() {
        var result = context.healthReportCommandService.handle(new GenerateHealthReportCommand(
                recipient, UUID.randomUUID(), "ON_DEMAND", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 1)));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");
    }

    @Test
    void weeklySummaryCoversTheLastSevenDaysAndIsAnnounced() {
        var device = context.linkDevice(recipient, "GP-0001");
        context.detect(device, VitalSignType.HR, "72", NOW.minusSeconds(3600));

        var report = HealthMonitoringTestContext.value(
                context.healthReportCommandService.handle(new CompileWeeklySummaryCommand(recipient)));

        assertThat(report.getReportType()).isEqualTo(HealthReportType.WEEKLY_AUTOMATIC);
        assertThat(report.getPeriod().startDate()).isEqualTo(LocalDate.of(2026, 9, 29));
        assertThat(report.getPeriod().endDate()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(context.eventsOfType(HealthReportCompiledIntegrationEvent.class)).hasSize(1);
    }
}
