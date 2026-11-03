package com.healthify.guardian.platform.healthmonitoring.infrastructure.scheduling;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.HealthReportCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.CompileWeeklySummaryCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Implements the <b>Weekly Compilation Policy</b>: every Sunday it compiles the automatic weekly
 * summary of every care recipient that has a linked wearable device (US24).
 */
@Slf4j
@Component
public class WeeklyHealthSummaryScheduler {

    private final WearableDeviceRepository wearableDeviceRepository;
    private final HealthReportCommandService healthReportCommandService;

    public WeeklyHealthSummaryScheduler(WearableDeviceRepository wearableDeviceRepository,
                                        HealthReportCommandService healthReportCommandService) {
        this.wearableDeviceRepository = wearableDeviceRepository;
        this.healthReportCommandService = healthReportCommandService;
    }

    @Scheduled(cron = "${health-monitoring.weekly-summary.cron:0 0 0 * * SUN}",
            zone = "${health-monitoring.zone-id:America/Lima}")
    public void compileWeeklySummaries() {
        wearableDeviceRepository.findAll().stream()
                .map(WearableDevice::getCareRecipientProfileId)
                .distinct()
                .forEach(careRecipientProfileId -> {
                    try {
                        var result = healthReportCommandService.handle(
                                new CompileWeeklySummaryCommand(careRecipientProfileId.value()));
                        if (result instanceof Result.Failure<?, ?> failure) {
                            log.info("Weekly summary skipped for {}: {}", careRecipientProfileId.value(), failure.error());
                        }
                    } catch (RuntimeException e) {
                        log.error("Weekly summary failed for {}", careRecipientProfileId.value(), e);
                    }
                });
    }
}
