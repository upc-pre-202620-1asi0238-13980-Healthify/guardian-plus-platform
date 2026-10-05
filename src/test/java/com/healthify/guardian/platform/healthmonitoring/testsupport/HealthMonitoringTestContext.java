package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices.HealthReportCommandServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices.VitalSignCommandServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices.VitalSignThresholdCommandServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices.VitalSignTypeCommandServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices.WearableDeviceCommandServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers.VitalSignsDetectedEventHandler;
import com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers.VitalSignsEmittedEventHandler;
import com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers.VitalSignsThresholdsEvaluatedEventHandler;
import com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers.WeeklySummaryCompiledEventHandler;
import com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices.VitalSignQueryServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.AssignWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsDetectedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsEmittedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsThresholdsEvaluatedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.WeeklySummaryCompiledEvent;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.configuration.VitalSignTypeCatalogInitializer;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Wires the real application services and event handlers of Health Monitoring over in-memory
 * repositories and a synchronous event bus, reproducing the Detect -> Emit -> Evaluate chain without
 * Spring or a database. Used by the sociable service tests and by the Gherkin step definitions.
 */
public class HealthMonitoringTestContext {

    public static final Instant NOW = Instant.parse("2026-10-05T15:00:00Z");

    public final List<Object> publishedEvents = new ArrayList<>();
    public final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final ApplicationEventPublisher eventBus = this::dispatch;

    public final InMemoryVitalSignRepository vitalSignRepository = new InMemoryVitalSignRepository(eventBus);
    public final InMemoryVitalSignThresholdRepository thresholdRepository = new InMemoryVitalSignThresholdRepository(eventBus);
    public final InMemoryWearableDeviceRepository wearableDeviceRepository = new InMemoryWearableDeviceRepository(eventBus);
    public final InMemoryVitalSignTypeRepository vitalSignTypeRepository = new InMemoryVitalSignTypeRepository(eventBus);
    public final InMemoryHealthReportRepository healthReportRepository = new InMemoryHealthReportRepository(eventBus);

    public final VitalSignCommandServiceImpl vitalSignCommandService = new VitalSignCommandServiceImpl(
            vitalSignRepository, thresholdRepository, wearableDeviceRepository, vitalSignTypeRepository, clock);
    public final VitalSignThresholdCommandServiceImpl thresholdCommandService =
            new VitalSignThresholdCommandServiceImpl(thresholdRepository, vitalSignTypeRepository);
    public final WearableDeviceCommandServiceImpl wearableDeviceCommandService =
            new WearableDeviceCommandServiceImpl(wearableDeviceRepository);
    public final VitalSignTypeCommandServiceImpl vitalSignTypeCommandService =
            new VitalSignTypeCommandServiceImpl(vitalSignTypeRepository);
    public final HealthReportCommandServiceImpl healthReportCommandService = new HealthReportCommandServiceImpl(
            healthReportRepository, vitalSignRepository, thresholdRepository, vitalSignTypeRepository, clock);
    public final VitalSignQueryServiceImpl vitalSignQueryService =
            new VitalSignQueryServiceImpl(vitalSignRepository, vitalSignTypeRepository);

    private final VitalSignsDetectedEventHandler detectedHandler = new VitalSignsDetectedEventHandler(vitalSignCommandService);
    private final VitalSignsEmittedEventHandler emittedHandler = new VitalSignsEmittedEventHandler(vitalSignCommandService);
    private final VitalSignsThresholdsEvaluatedEventHandler evaluatedHandler = new VitalSignsThresholdsEvaluatedEventHandler(
            vitalSignRepository, thresholdRepository, vitalSignTypeRepository, eventBus);
    private final WeeklySummaryCompiledEventHandler weeklySummaryHandler = new WeeklySummaryCompiledEventHandler(eventBus);

    private void dispatch(Object event) {
        publishedEvents.add(event);
        switch (event) {
            case VitalSignsDetectedEvent detected -> detectedHandler.on(detected);
            case VitalSignsEmittedEvent emitted -> emittedHandler.on(emitted);
            case VitalSignsThresholdsEvaluatedEvent evaluated -> evaluatedHandler.on(evaluated);
            case WeeklySummaryCompiledEvent compiled -> weeklySummaryHandler.on(compiled);
            default -> {
            }
        }
    }

    public <T> List<T> eventsOfType(Class<T> type) {
        return publishedEvents.stream().filter(type::isInstance).map(type::cast).toList();
    }

    /**
     * Registers a type with the reference ranges of the seeded catalog entry of the same code, or with
     * wide ranges for any other code.
     */
    public VitalSignType registerType(String code, String name, String unit) {
        var defaults = VitalSignTypeCatalogInitializer.DEFAULT_TYPES.stream()
                .filter(type -> type.code().equals(code))
                .findFirst();
        return registerType(new RegisterVitalSignTypeCommand(code, name, unit,
                defaults.map(RegisterVitalSignTypeCommand::normalMinimum).orElse(BigDecimal.ZERO),
                defaults.map(RegisterVitalSignTypeCommand::normalMaximum).orElse(new BigDecimal("1000")),
                defaults.map(RegisterVitalSignTypeCommand::physicalMinimum).orElse(BigDecimal.ZERO),
                defaults.map(RegisterVitalSignTypeCommand::physicalMaximum).orElse(new BigDecimal("1000"))));
    }

    public VitalSignType registerType(RegisterVitalSignTypeCommand command) {
        return value(vitalSignTypeCommandService.handle(command));
    }

    public WearableDevice assignDevice(UUID careRecipientProfileId, String serialNumber) {
        return value(wearableDeviceCommandService.handle(
                new AssignWearableDeviceCommand(careRecipientProfileId, serialNumber, "WRISTBAND")));
    }

    public VitalSignThreshold defineThreshold(UUID careRecipientProfileId, VitalSignType type,
                                              String minimum, String maximum, int requiredHits) {
        return value(thresholdCommandService.handle(new DefineVitalSignThresholdCommand(
                careRecipientProfileId, type.getId().value(), new BigDecimal(minimum), new BigDecimal(maximum), requiredHits)));
    }

    public Result<VitalSign, ?> detect(WearableDevice device, VitalSignType type, String value, Instant measuredAt) {
        return vitalSignCommandService.handle(new DetectVitalSignsCommand(
                device.getId().value(), device.getCareRecipientProfileId().value(), type.getId().value(),
                new BigDecimal(value), measuredAt, NOW));
    }

    public static <T> T value(Result<T, ?> result) {
        if (result instanceof Result.Success<T, ?> success) {
            return success.value();
        }
        throw new AssertionError("Expected success but was " + result);
    }
}
