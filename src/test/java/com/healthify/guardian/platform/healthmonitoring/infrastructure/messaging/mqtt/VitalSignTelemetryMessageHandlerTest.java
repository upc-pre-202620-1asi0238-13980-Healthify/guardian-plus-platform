package com.healthify.guardian.platform.healthmonitoring.infrastructure.messaging.mqtt;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext.NOW;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of {@link VitalSignTelemetryMessageHandler} with the payloads published by the IoT simulator.
 */
class VitalSignTelemetryMessageHandlerTest {

    private static final String TOPIC = "guardian/vitals/device";

    private final HealthMonitoringTestContext context = new HealthMonitoringTestContext();
    private final VitalSignTelemetryMessageHandler handler =
            new VitalSignTelemetryMessageHandler(context.vitalSignCommandService);
    private WearableDevice device;

    @BeforeEach
    void linkDevice() {
        device = context.linkDevice(UUID.randomUUID(), "GP-WB-001");
    }

    private static byte[] reading(Object deviceId, Object recipient, String type, String value) {
        // same shape as the simulator's Signal.to_dict(), including the fields Health Monitoring ignores
        return """
                {"deviceId":"%s","careRecipientProfileId":"%s","signalType":"VITAL_SIGN_READING",
                 "severity":"INFO","measuredAt":"2026-10-05T14:59:50.423159+00:00","vitalSignTypeCode":"%s",
                 "vitalSignTypeName":"Frecuencia cardiaca","value":%s,"unit":"bpm","status":"NORMAL",
                 "normalRange":{"min":60,"max":100},"sustained":false}
                """.formatted(deviceId, recipient, type, value).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void storesAReadingPublishedByTheSimulator() {
        var result = handler.handle(TOPIC,
                reading(device.getId().value(), device.getCareRecipientProfileId().value(), "HR", "72.0"), NOW);

        assertThat(result).hasValueSatisfying(r -> assertThat(r).isInstanceOf(Result.Success.class));
        var stored = context.vitalSignRepository.findAll();
        assertThat(stored).hasSize(1);
        assertThat(stored.getFirst().getVitalSignType()).isEqualTo(VitalSignType.HR);
        assertThat(stored.getFirst().getValue().value()).isEqualByComparingTo(new BigDecimal("72.0"));
        assertThat(stored.getFirst().getMeasuredAt()).isEqualTo(Instant.parse("2026-10-05T14:59:50.423159Z"));
        assertThat(stored.getFirst().getReceivedAt()).isEqualTo(NOW);
    }

    @Test
    void ignoresSignalsThatAreNotVitalSignReadings() {
        var fall = """
                {"deviceId":"%s","careRecipientProfileId":"%s","signalType":"FALL_DETECTED","severity":"CRITICAL",
                 "measuredAt":"2026-10-05T14:59:50+00:00","impactG":3.18}
                """.formatted(device.getId().value(), device.getCareRecipientProfileId().value());

        assertThat(handler.handle(TOPIC, fall.getBytes(StandardCharsets.UTF_8), NOW)).isEmpty();
        assertThat(context.vitalSignRepository.findAll()).isEmpty();
    }

    @Test
    void dropsMalformedJsonWithoutThrowing() {
        assertThat(handler.handle(TOPIC, "{not json".getBytes(StandardCharsets.UTF_8), NOW)).isEmpty();
        assertThat(context.vitalSignRepository.findAll()).isEmpty();
    }

    @Test
    void dropsReadingsOfDevicesNotIdentifiedByUuid() {
        // devices loaded by hand in the simulator (`devices GP-WB-001`) are not linked in the backend
        var result = handler.handle(TOPIC,
                reading("GP-WB-001", device.getCareRecipientProfileId().value(), "HR", "72.0"), NOW);

        assertThat(result).isEmpty();
        assertThat(context.vitalSignRepository.findAll()).isEmpty();
    }

    @Test
    void reportsTheFailureOfAReadingFromAnUnlinkedDevice() {
        var result = handler.handle(TOPIC, reading(UUID.randomUUID(), UUID.randomUUID(), "SPO2", "97.0"), NOW);

        assertThat(result).hasValueSatisfying(r -> assertThat(r).isInstanceOf(Result.Failure.class));
        assertThat(context.vitalSignRepository.findAll()).isEmpty();
    }
}
