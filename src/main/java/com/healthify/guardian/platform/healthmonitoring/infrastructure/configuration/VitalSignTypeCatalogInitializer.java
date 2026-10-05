package com.healthify.guardian.platform.healthmonitoring.infrastructure.configuration;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignTypeCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the vital sign type catalog with the five vital signs monitored by the wearable
 * (US01-US05) when they are missing, so the platform is usable right after deployment.
 */
@Slf4j
@Component
public class VitalSignTypeCatalogInitializer {

    static final List<RegisterVitalSignTypeCommand> DEFAULT_TYPES = List.of(
            new RegisterVitalSignTypeCommand("HR", "Heart rate", "bpm"),
            new RegisterVitalSignTypeCommand("BP_SYS", "Systolic blood pressure", "mmHg"),
            new RegisterVitalSignTypeCommand("BP_DIA", "Diastolic blood pressure", "mmHg"),
            new RegisterVitalSignTypeCommand("SPO2", "Peripheral oxygen saturation", "%"),
            new RegisterVitalSignTypeCommand("TEMP", "Body temperature", "°C"),
            new RegisterVitalSignTypeCommand("RESP_RATE", "Respiratory rate", "rpm"));

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
        DEFAULT_TYPES.stream()
                .filter(type -> vitalSignTypeRepository.findByCode(new VitalSignTypeCode(type.code())).isEmpty())
                .forEach(type -> {
                    vitalSignTypeCommandService.handle(type);
                    log.info("Seeded vital sign type {}", type.code());
                });
    }
}
