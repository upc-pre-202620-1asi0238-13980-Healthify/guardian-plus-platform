package com.healthify.guardian.platform.careroutineswellness.infrastructure.messaging.mqtt;

import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.ActivityTelemetryConsumer;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.ActivityTelemetryMessage;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.SleepTelemetryConsumer;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.SleepTelemetryMessage;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

/**
 * Deserializes the raw activity and sleep telemetry received from the broker and hands it to the
 * Anti-Corruption Layer consumers. It is independent of the MQTT client so it can be tested without a broker.
 *
 * <p>It never throws: a malformed message or a rejected sample is logged and dropped, because an exception
 * would make the MQTT client close the connection and lose the rest of the stream.</p>
 */
@Slf4j
@Component
public class WearableTelemetryMessageHandler {

    // Private mapper: the wearable's wire format must not depend on the REST API's Jackson customizations
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final ActivityTelemetryConsumer activityTelemetryConsumer;
    private final SleepTelemetryConsumer sleepTelemetryConsumer;

    public WearableTelemetryMessageHandler(
            ActivityTelemetryConsumer activityTelemetryConsumer, SleepTelemetryConsumer sleepTelemetryConsumer) {
        this.activityTelemetryConsumer = activityTelemetryConsumer;
        this.sleepTelemetryConsumer = sleepTelemetryConsumer;
    }

    /**
     * @param topic   topic the message arrived on, only used for logging
     * @param payload raw JSON payload
     * @return true when the message was turned into a command that succeeded
     */
    public boolean handleActivity(String topic, byte[] payload) {
        return handle(topic, payload, ActivityTelemetryMessage.class, activityTelemetryConsumer::consume);
    }

    /**
     * @param topic   topic the message arrived on, only used for logging
     * @param payload raw JSON payload
     * @return true when the message was turned into a command that succeeded
     */
    public boolean handleSleep(String topic, byte[] payload) {
        return handle(topic, payload, SleepTelemetryMessage.class, sleepTelemetryConsumer::consume);
    }

    private <M> boolean handle(
            String topic, byte[] payload, Class<M> type, Function<M, ? extends Optional<? extends Result<?, ?>>> consumer) {
        try {
            var message = JSON.readValue(new String(payload, StandardCharsets.UTF_8), type);
            var result = consumer.apply(message);
            if (result.isEmpty()) {
                log.debug("Ignored message on {}: not consumed by Care Routines & Wellness", topic);
                return false;
            }
            if (result.get() instanceof Result.Failure<?, ?> failure) {
                log.warn("Telemetry on {} rejected: {}", topic, failure.error());
                return false;
            }
            return true;
        } catch (JacksonException | IllegalArgumentException e) {
            log.warn("Malformed telemetry message on {}: {}", topic, e.getMessage());
            return false;
        } catch (RuntimeException e) {
            log.error("Telemetry message on {} could not be processed", topic, e);
            return false;
        }
    }
}
