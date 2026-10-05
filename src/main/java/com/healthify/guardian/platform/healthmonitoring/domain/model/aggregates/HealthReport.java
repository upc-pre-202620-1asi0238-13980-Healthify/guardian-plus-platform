package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.GenerateHealthReportCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.entities.VitalSignSummary;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.HealthReportGeneratedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.WeeklySummaryCompiledEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Aggregate root consolidating the vital sign time series of a care recipient within a period
 * into one {@link VitalSignSummary} per vital sign type.
 */
@Getter
public class HealthReport extends AbstractDomainAggregateRoot<HealthReport> {

    private static final String REPORT_TYPE_INVALID_KEY = "health-report.report-type.invalid";
    private static final String VITAL_SIGNS_EMPTY_KEY = "health-report.vital-signs.empty";
    private static final String OUT_OF_SCOPE_KEY = "health-report.vital-sign.out-of-scope";

    private HealthReportId id;
    private CareRecipientProfileId careRecipientProfileId;
    private UserId generatedByUserId;
    private HealthReportType reportType;
    private DateRange period;
    private List<VitalSignSummary> summaries = new ArrayList<>();
    private Integer recurrentAnomaliesCount;
    private Instant generatedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public HealthReport() {
    }

    /**
     * Compiles a report. Registers {@link HealthReportGeneratedEvent} and, for automatic weekly
     * reports, {@link WeeklySummaryCompiledEvent}.
     *
     * @param command     the report request; {@code generatedByUserId} is null for automatic reports
     * @param vitalSigns  the readings of the care recipient within the period, never empty
     * @param thresholds  the active thresholds of the care recipient, used to count anomalies
     * @param typeLabeler resolves the label (catalog code) of each vital sign type
     */
    public HealthReport(GenerateHealthReportCommand command,
                        List<VitalSign> vitalSigns,
                        List<VitalSignThreshold> thresholds,
                        Function<VitalSignTypeId, String> typeLabeler) {
        if (vitalSigns == null || vitalSigns.isEmpty()) {
            throw new IllegalArgumentException(VITAL_SIGNS_EMPTY_KEY);
        }
        this.careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
        if (vitalSigns.stream().anyMatch(vitalSign -> !vitalSign.getCareRecipientProfileId().equals(careRecipientProfileId))) {
            throw new IllegalArgumentException(OUT_OF_SCOPE_KEY);
        }
        this.id = HealthReportId.generate();
        this.generatedByUserId = command.generatedByUserId() == null ? null : new UserId(command.generatedByUserId());
        this.reportType = parseReportType(command.reportType());
        this.period = new DateRange(command.periodStart(), command.periodEnd());
        this.summaries = summarize(vitalSigns, thresholds, typeLabeler);
        this.recurrentAnomaliesCount = (int) summaries.stream().filter(VitalSignSummary::isRecurrent).count();
        this.generatedAt = Instant.now();

        registerDomainEvent(new HealthReportGeneratedEvent(id, careRecipientProfileId, reportType, generatedAt));
        if (reportType == HealthReportType.WEEKLY_AUTOMATIC) {
            registerDomainEvent(new WeeklySummaryCompiledEvent(
                    id, careRecipientProfileId, period, recurrentAnomaliesCount, generatedAt));
        }
    }

    /** A report is clinically stable when no vital sign type left its clinical range. */
    public boolean isClinicallyStable() {
        return summaries.stream().allMatch(VitalSignSummary::isStable);
    }

    public List<VitalSignSummary> getSummaries() {
        return Collections.unmodifiableList(summaries);
    }

    private static List<VitalSignSummary> summarize(List<VitalSign> vitalSigns,
                                                    List<VitalSignThreshold> thresholds,
                                                    Function<VitalSignTypeId, String> typeLabeler) {
        var thresholdsByType = (thresholds == null ? List.<VitalSignThreshold>of() : thresholds).stream()
                .filter(VitalSignThreshold::isActive)
                .collect(Collectors.toMap(VitalSignThreshold::getVitalSignTypeId, threshold -> threshold, (a, b) -> a));
        var readingsByType = vitalSigns.stream()
                .collect(Collectors.groupingBy(VitalSign::getVitalSignTypeId, LinkedHashMap::new, Collectors.toList()));

        var summaries = new ArrayList<VitalSignSummary>();
        long position = 1;
        for (var entry : readingsByType.entrySet()) {
            summaries.add(VitalSignSummary.of(position++, typeLabeler.apply(entry.getKey()),
                    entry.getValue(), thresholdsByType.get(entry.getKey())));
        }
        return summaries;
    }

    private static HealthReportType parseReportType(String reportType) {
        if (reportType == null) {
            throw new IllegalArgumentException(REPORT_TYPE_INVALID_KEY);
        }
        try {
            return HealthReportType.valueOf(reportType.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(REPORT_TYPE_INVALID_KEY);
        }
    }

    /** Restores state from persistence. Used by the persistence assembler. */
    public void setId(HealthReportId id) {
        this.id = id;
    }

    public void setCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        this.careRecipientProfileId = careRecipientProfileId;
    }

    public void setGeneratedByUserId(UserId generatedByUserId) {
        this.generatedByUserId = generatedByUserId;
    }

    public void setReportType(HealthReportType reportType) {
        this.reportType = reportType;
    }

    public void setPeriod(DateRange period) {
        this.period = period;
    }

    public void setSummaries(List<VitalSignSummary> summaries) {
        this.summaries = new ArrayList<>(summaries);
    }

    public void setRecurrentAnomaliesCount(Integer recurrentAnomaliesCount) {
        this.recurrentAnomaliesCount = recurrentAnomaliesCount;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
