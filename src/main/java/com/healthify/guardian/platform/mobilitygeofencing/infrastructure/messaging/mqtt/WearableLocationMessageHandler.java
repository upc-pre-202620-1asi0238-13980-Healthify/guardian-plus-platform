package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.messaging.mqtt;

import com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.WearableLocationConsumer;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.WearableLocationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

/**
 * Deserializes the raw location telemetry received from the broker and hands it to
 * {@link WearableLocationConsumer}. It is independent of the MQTT client so it can be tested without a broker.
 *
 * <p>It never throws: a malformed message or a rejected location is logged and dropped, because an exception
 * would make the MQTT client close the connection and lose the rest of the stream.</p>
 */
@Slf4j
@Component
public class WearableLocationMessageHandler {

    // Private mapper: the wearable's wire format must not depend on the REST API's Jackson customizations
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final WearableLocationConsumer wearableLocationConsumer;

    public WearableLocationMessageHandler(WearableLocationConsumer wearableLocationConsumer) {
        this.wearableLocationConsumer = wearableLocationConsumer;
    }

    /**
     * @param topic   topic the message arrived on, only used for logging
     * @param payload raw JSON payload
     * @return true when the location was recorded
     */
    public boolean handleLocation(String topic, byte[] payload) {
        try {
            var message = JSON.readValue(new String(payload, StandardCharsets.UTF_8), WearableLocationMessage.class);
            wearableLocationConsumer.consumeLocation(message);
            return true;
        } catch (JacksonException | IllegalArgumentException e) {
            log.warn("Malformed location message on {}: {}", topic, e.getMessage());
            return false;
        } catch (RuntimeException e) {
            log.error("Location message on {} could not be processed", topic, e);
            return false;
        }
    }
}
