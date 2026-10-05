package com.healthify.guardian.platform.healthmonitoring.infrastructure.configuration;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignTypeCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the vital sign type catalog with the six vital signs monitored by the wearable
 * (US01-US05) when they are missing, so the platform is usable right after deployment.
 *
 * <p>Each type comes with its adult resting normal range and the physical limits a real reading
 * can take. Types seeded before reference ranges existed are completed in place.</p>
 */
@Slf4j
@Component
public class VitalSignTypeCatalogInitializer {

    public static final List<RegisterVitalSignTypeCommand> DEFAULT_TYPES = List.of(
            type("HR", "Heart rate", "bpm", "60", "100", "20", "250"),
            type("BP_SYS", "Systolic blood pressure", "mmHg", "90", "140", "50", "260"),
            type("BP_DIA", "Diastolic blood pressure", "mmHg", "60", "90", "30", "160"),
            type("SPO2", "Peripheral oxygen saturation", "%", "92", "100", "0", "100"),
            type("TEMP", "Body temperature", "°C", "36.0", "37.5", "30.0", "43.0"),
            type("RESP_RATE", "Respiratory rate", "rpm", "12", "20", "4", "60"));

    private final VitalSignTypeRepository vitalSignTypeRepository;
    private final VitalSignTypeCommandService vitalSignTypeCommandService;
    private final boolean enabled;

    public VitalSignTypeCatalogInitializer(VitalSignTypeRepository vitalSignTypeRepository,
                                           VitalSignTypeCommandService vitalSignTypeCommandService,
                                           @Value("${health-monitoring.catalog.seed-defaults:true}") boolean enabled) {
        this.vitalSignTypeRepository = vitalSignTypeRepository;
        this.vitalSignTypeCommandService = vitalSignTypeCommandService;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaultVitalSignTypes() {
        if (!enabled) {
            return;
        }
        DEFAULT_TYPES.forEach(defaults -> vitalSignTypeRepository.findByCode(new VitalSignTypeCode(defaults.code()))
                .ifPresentOrElse(existing -> {
                    if (!existing.hasReferenceRanges()) {
                        existing.defineReferenceRanges(
                                new VitalSignRange(defaults.normalMinimum(), defaults.normalMaximum()),
                                new VitalSignRange(defaults.physicalMinimum(), defaults.physicalMaximum()));
                        vitalSignTypeRepository.save(existing);
                        log.info("Completed reference ranges of vital sign type {}", defaults.code());
                    }
                }, () -> {
                    vitalSignTypeCommandService.handle(defaults);
                    log.info("Seeded vital sign type {}", defaults.code());
                }));
    }

    private static RegisterVitalSignTypeCommand type(String code, String name, String unit,
                                                     String normalMinimum, String normalMaximum,
                                                     String physicalMinimum, String physicalMaximum) {
        return new RegisterVitalSignTypeCommand(code, name, unit,
                new BigDecimal(normalMinimum), new BigDecimal(normalMaximum),
                new BigDecimal(physicalMinimum), new BigDecimal(physicalMaximum));
    }
}
