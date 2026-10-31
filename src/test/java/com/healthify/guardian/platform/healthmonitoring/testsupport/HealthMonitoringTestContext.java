package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices.HealthReportCommandServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices.VitalSignCommandServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices.WearableDeviceCommandServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers.VitalSignsDetectedEventHandler;
import com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers.VitalSignsEmittedEventHandler;
import com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers.VitalSignsThresholdsEvaluatedEventHandler;
import com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers.WeeklySummaryCompiledEventHandler;
import com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices.VitalSignQueryServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.LinkWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsDetectedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsEmittedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsThresholdsEvaluatedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.WeeklySummaryCompiledEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
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
    public final InMemoryWearableDeviceRepository wearableDeviceRepository = new InMemoryWearableDeviceRepository(eventBus);
    public final InMemoryHealthReportRepository healthReportRepository = new InMemoryHealthReportRepository(eventBus);

    public final VitalSignCommandServiceImpl vitalSignCommandService =
            new VitalSignCommandServiceImpl(vitalSignRepository, wearableDeviceRepository, clock);
    public final WearableDeviceCommandServiceImpl wearableDeviceCommandService =
            new WearableDeviceCommandServiceImpl(wearableDeviceRepository);
    public final HealthReportCommandServiceImpl healthReportCommandService =
            new HealthReportCommandServiceImpl(healthReportRepository, vitalSignRepository, clock);
    public final VitalSignQueryServiceImpl vitalSignQueryService = new VitalSignQueryServiceImpl(vitalSignRepository);

    private final VitalSignsDetectedEventHandler detectedHandler = new VitalSignsDetectedEventHandler(vitalSignCommandService);
    private final VitalSignsEmittedEventHandler emittedHandler = new VitalSignsEmittedEventHandler(vitalSignCommandService);
    private final VitalSignsThresholdsEvaluatedEventHandler evaluatedHandler =
            new VitalSignsThresholdsEvaluatedEventHandler(vitalSignRepository, eventBus);
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

    public WearableDevice linkDevice(UUID careRecipientProfileId, String serialNumber) {
        return value(wearableDeviceCommandService.handle(
                new LinkWearableDeviceCommand(careRecipientProfileId, serialNumber, "WRISTBAND")));
    }

    public Result<VitalSign, ?> detect(WearableDevice device, VitalSignType type, String value, Instant measuredAt) {
        return vitalSignCommandService.handle(new DetectVitalSignsCommand(
                device.getId().value(), device.getCareRecipientProfileId().value(), type.code(),
                new BigDecimal(value), measuredAt, NOW));
    }

    public static <T> T value(Result<T, ?> result) {
        if (result instanceof Result.Success<T, ?> success) {
            return success.value();
        }
        throw new AssertionError("Expected success but was " + result);
    }
}
