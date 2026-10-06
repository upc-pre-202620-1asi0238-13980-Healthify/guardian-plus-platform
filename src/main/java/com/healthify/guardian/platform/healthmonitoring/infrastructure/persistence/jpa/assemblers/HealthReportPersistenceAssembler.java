package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.entities.VitalSignSummary;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.HealthReportPersistenceEntity;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Static assembler between the {@link HealthReport} aggregate and its persistence entity.
 * Serializes {@code summaries} and {@code recurrentAnomaliesCount} into the {@code summary} column.
 */
public final class HealthReportPersistenceAssembler {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private HealthReportPersistenceAssembler() {
    }

    /** JSON shape of the {@code summary} column. */
    record SummaryDocument(List<SummaryItem> summaries, Integer recurrentAnomaliesCount) {
    }

    record SummaryItem(Long id, String metricType, Double averageValue, Double minValue, Double maxValue,
                       Integer readingsCount, Integer outOfRangeCount, String stabilityIndex) {
    }

    public static HealthReport toDomainFromPersistence(HealthReportPersistenceEntity entity) {
        if (entity == null) return null;
        var document = JSON.readValue(entity.getSummary(), SummaryDocument.class);
        var report = new HealthReport();
        report.setId(new HealthReportId(entity.getId()));
        report.setCareRecipientProfileId(entity.getCareRecipientProfileId());
        report.setGeneratedByUserId(entity.getGeneratedByUserId() == null ? null : new UserId(entity.getGeneratedByUserId()));
        report.setReportType(entity.getReportType());
        report.setPeriod(new DateRange(entity.getPeriodStart(), entity.getPeriodEnd()));
        report.setSummaries(document.summaries().stream()
                .map(item -> new VitalSignSummary(item.id(), item.metricType(), item.averageValue(), item.minValue(),
                        item.maxValue(), item.readingsCount(), item.outOfRangeCount(), item.stabilityIndex()))
                .toList());
        report.setRecurrentAnomaliesCount(document.recurrentAnomaliesCount());
        report.setGeneratedAt(entity.getGeneratedAt());
        return report;
    }

    public static HealthReportPersistenceEntity toPersistenceFromDomain(HealthReport report) {
        if (report == null) return null;
        var entity = new HealthReportPersistenceEntity();
        entity.setId(report.getId().value());
        entity.setCareRecipientProfileId(report.getCareRecipientProfileId());
        entity.setGeneratedByUserId(report.getGeneratedByUserId() == null ? null : report.getGeneratedByUserId().value());
        entity.setReportType(report.getReportType());
        entity.setPeriodStart(report.getPeriod().startDate());
        entity.setPeriodEnd(report.getPeriod().endDate());
        entity.setSummary(JSON.writeValueAsString(new SummaryDocument(
                report.getSummaries().stream()
                        .map(summary -> new SummaryItem(summary.getId(), summary.getMetricType(), summary.getAverageValue(),
                                summary.getMinValue(), summary.getMaxValue(), summary.getReadingsCount(),
                                summary.getOutOfRangeCount(), summary.getStabilityIndex()))
                        .toList(),
                report.getRecurrentAnomaliesCount())));
        entity.setGeneratedAt(report.getGeneratedAt());
        return entity;
    }
}
