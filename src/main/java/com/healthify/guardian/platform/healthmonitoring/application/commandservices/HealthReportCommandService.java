package com.healthify.guardian.platform.healthmonitoring.application.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.CompileWeeklySummaryCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.GenerateHealthReportCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code HealthReport} aggregate.
 */
public interface HealthReportCommandService {

    /**
     * Compiles a report from the readings of the requested period.
     */
    Result<HealthReport, ApplicationError> handle(GenerateHealthReportCommand command);

    /**
     * Compiles the automatic weekly summary covering the last seven days.
     */
    Result<HealthReport, ApplicationError> handle(CompileWeeklySummaryCommand command);
}
