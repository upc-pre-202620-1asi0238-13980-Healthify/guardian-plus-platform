package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.HealthReportRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InMemoryHealthReportRepository implements HealthReportRepository {

    private final InMemoryAggregateStore<HealthReportId, HealthReport> store;

    public InMemoryHealthReportRepository(ApplicationEventPublisher eventPublisher) {
        this.store = new InMemoryAggregateStore<>(HealthReport::getId, eventPublisher);
    }

    @Override
    public HealthReport save(HealthReport report) {
        return store.save(report);
    }

    @Override
    public Optional<HealthReport> findById(HealthReportId id) {
        return store.findById(id);
    }

    @Override
    public List<HealthReport> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        return store.findAll(report -> report.getCareRecipientProfileId().equals(careRecipientProfileId)).stream()
                .sorted(Comparator.comparing(HealthReport::getGeneratedAt).reversed()).toList();
    }
}
