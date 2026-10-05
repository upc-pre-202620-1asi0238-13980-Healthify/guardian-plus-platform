package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.HealthReportCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.CompileWeeklySummaryCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.GenerateHealthReportCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.HealthReportRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignThresholdRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Application service that compiles health reports, on demand or as the automatic weekly summary.
 */
@Service
public class HealthReportCommandServiceImpl implements HealthReportCommandService {

    private static final String NO_READINGS_KEY = "health-report.vital-signs.empty";
    private static final int WEEK_DAYS = 7;

    private final HealthReportRepository healthReportRepository;
    private final VitalSignRepository vitalSignRepository;
    private final VitalSignThresholdRepository vitalSignThresholdRepository;
    private final VitalSignTypeRepository vitalSignTypeRepository;
    private final Clock clock;

    @Autowired
    public HealthReportCommandServiceImpl(HealthReportRepository healthReportRepository,
                                          VitalSignRepository vitalSignRepository,
                                          VitalSignThresholdRepository vitalSignThresholdRepository,
                                          VitalSignTypeRepository vitalSignTypeRepository) {
        this(healthReportRepository, vitalSignRepository, vitalSignThresholdRepository, vitalSignTypeRepository,
                Clock.systemUTC());
    }

    public HealthReportCommandServiceImpl(HealthReportRepository healthReportRepository,
                                          VitalSignRepository vitalSignRepository,
                                          VitalSignThresholdRepository vitalSignThresholdRepository,
                                          VitalSignTypeRepository vitalSignTypeRepository,
                                          Clock clock) {
        this.healthReportRepository = healthReportRepository;
        this.vitalSignRepository = vitalSignRepository;
        this.vitalSignThresholdRepository = vitalSignThresholdRepository;
        this.vitalSignTypeRepository = vitalSignTypeRepository;
        this.clock = clock;
    }

    @Override
    public Result<HealthReport, ApplicationError> handle(GenerateHealthReportCommand command) {
        try {
            var careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
            var period = new DateRange(command.periodStart(), command.periodEnd());
            var vitalSigns = vitalSignRepository.findByCareRecipientProfileIdAndPeriod(careRecipientProfileId, period);
            if (vitalSigns.isEmpty()) {
                return Result.failure(ApplicationError.businessRuleViolation(
                        "generate-health-report", MessageResolver.resolveOrDefault(NO_READINGS_KEY, NO_READINGS_KEY)));
            }
            var thresholds = vitalSignThresholdRepository.findAllActiveByCareRecipientProfileId(careRecipientProfileId);
            var typeCodes = vitalSignTypeRepository.findAll().stream()
                    .collect(Collectors.toMap(VitalSignType::getId, type -> type.getCode().value()));
            var report = new HealthReport(command, vitalSigns, thresholds,
                    (VitalSignTypeId typeId) -> typeCodes.getOrDefault(typeId, typeId.value().toString()));
            return Result.success(healthReportRepository.save(report));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "generate-health-report", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }

    @Override
    public Result<HealthReport, ApplicationError> handle(CompileWeeklySummaryCommand command) {
        var today = LocalDate.now(clock);
        return handle(new GenerateHealthReportCommand(
                command.careRecipientProfileId(),
                null,
                HealthReportType.WEEKLY_AUTOMATIC.name(),
                today.minusDays(WEEK_DAYS - 1),
                today));
    }
}
