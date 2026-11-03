package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.messaging.mqtt;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.ReceiveLocationCommand;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.WearableLocationConsumer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of {@link WearableLocationMessageHandler} with the payloads published by the IoT simulator.
 */
class WearableLocationMessageHandlerTest {

    private static final String TOPIC = "guardian/location/GP-WB-001";
    private static final UUID PERSON = UUID.randomUUID();

    private final List<ReceiveLocationCommand> received = new ArrayList<>();
    private final WearableLocationMessageHandler handler =
            new WearableLocationMessageHandler(new WearableLocationConsumer(received::add));

    // same shape as the simulator's Signal.to_dict(), including the fields this context ignores
    private static byte[] location(String recipient) {
        return """
                {"deviceId":"GP-WB-001","careRecipientProfileId":"%s","signalType":"LOCATION_UPDATE",
                 "severity":"INFO","measuredAt":"2026-10-22T15:00:00.123456+00:00","latitude":-12.046374,
                 "longitude":-77.042793,"accuracyInMeters":8.4,"recordedAt":"2026-10-22T15:00:00.123456+00:00",
                 "speedMps":0.0,"distanceFromHomeMeters":12.5,"outsideSafeZone":false}
                """.formatted(recipient).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void theSimulatorLocationIsRecordedForTheCareRecipient() {
        assertThat(handler.handleLocation(TOPIC, location(PERSON.toString()))).isTrue();

        assertThat(received).hasSize(1);
        var command = received.getFirst();
        assertThat(command.fragileCitizenId()).isEqualTo(PERSON);
        assertThat(command.coordinates().latitude()).isEqualTo(-12.046374);
        assertThat(command.coordinates().longitude()).isEqualTo(-77.042793);
        assertThat(command.accuracyInMeters()).isEqualTo(8.4);
        assertThat(command.recordedAt()).isEqualTo(Instant.parse("2026-10-22T15:00:00.123456Z"));
    }

    @Test
    void aMalformedMessageIsDroppedWithoutThrowing() {
        assertThat(handler.handleLocation(TOPIC, "not json".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(received).isEmpty();
    }

    @Test
    void aMessageWithoutThePersonIsDropped() {
        assertThat(handler.handleLocation(TOPIC, location("not-a-uuid"))).isFalse();
        assertThat(handler.handleLocation(TOPIC,
                "{\"latitude\":-12.0,\"longitude\":-77.0}".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(received).isEmpty();
    }
}
