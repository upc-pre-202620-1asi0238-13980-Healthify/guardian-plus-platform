package com.healthify.guardian.platform.careroutineswellness.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.ActivityTelemetryConsumer;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.ActivityTelemetryMessage;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.SleepTelemetryConsumer;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.SleepTelemetryMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Technical connection to the wearable device's MQTT broker.
 *
 * <p>Subscribes to the activity/inactivity and sleep telemetry topics and hands each message to
 * {@link ActivityTelemetryConsumer} / {@link SleepTelemetryConsumer} respectively, deserializing
 * the JSON payload into this bounded context's own telemetry message records.</p>
 *
 * <p><b>Wiring status:</b> the wearable device and its simulator are being built separately; the
 * actual MQTT client subscription is intentionally left as a follow-up (see
 * {@code care-routines-wellness.wearable.mqtt.enabled}, off by default) so this class does not
 * attempt a broker connection before one exists. {@link #onActivityMessage(String)} and
 * {@link #onSleepMessage(String)} are already wired end-to-end and ready to be invoked by
 * whichever MQTT client library is added once the broker is available, or directly by the
 * wearable simulator via a lightweight adapter.</p>
 */
@Slf4j
@Component
public class WearableTelemetryBrokerAdapter {

    private final ActivityTelemetryConsumer activityTelemetryConsumer;
    private final SleepTelemetryConsumer sleepTelemetryConsumer;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String brokerUrl;
    private final String activityTopic;
    private final String sleepTopic;

    public WearableTelemetryBrokerAdapter(
            ActivityTelemetryConsumer activityTelemetryConsumer,
            SleepTelemetryConsumer sleepTelemetryConsumer,
            @Value("${care-routines-wellness.wearable.mqtt.enabled:false}") boolean enabled,
            @Value("${care-routines-wellness.wearable.mqtt.broker-url:tcp://localhost:1883}") String brokerUrl,
            @Value("${care-routines-wellness.wearable.mqtt.activity-topic:guardian-plus/wearable/activity}") String activityTopic,
            @Value("${care-routines-wellness.wearable.mqtt.sleep-topic:guardian-plus/wearable/sleep}") String sleepTopic) {
        this.activityTelemetryConsumer = activityTelemetryConsumer;
        this.sleepTelemetryConsumer = sleepTelemetryConsumer;
        // A private ObjectMapper is used instead of the application's shared bean: this adapter's
        // JSON contract is purely internal (the wearable's wire format), so it must not be affected
        // by global Jackson customizations configured for the public REST API.
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.enabled = enabled;
        this.brokerUrl = brokerUrl;
        this.activityTopic = activityTopic;
        this.sleepTopic = sleepTopic;
    }

    @PostConstruct
    void logWiringStatus() {
        if (enabled) {
            log.warn("Wearable MQTT ingestion is enabled ({}, topics: {}, {}) but no MQTT client is wired yet; "
                            + "messages must currently be delivered through onActivityMessage/onSleepMessage directly.",
                    brokerUrl, activityTopic, sleepTopic);
        } else {
            log.info("Wearable MQTT ingestion is disabled (care-routines-wellness.wearable.mqtt.enabled=false); "
                    + "Care Routines & Wellness is ready to receive telemetry once the wearable simulator is wired up.");
        }
    }

    /**
     * Handles a raw activity/inactivity telemetry message received from the broker's
     * {@link #activityTopic}.
     *
     * @param jsonPayload the raw JSON payload matching {@link ActivityTelemetryMessage}
     */
    public void onActivityMessage(String jsonPayload) {
        activityTelemetryConsumer.consume(readPayload(jsonPayload, ActivityTelemetryMessage.class));
    }

    /**
     * Handles a raw closed sleep-cycle telemetry message received from the broker's
     * {@link #sleepTopic}.
     *
     * @param jsonPayload the raw JSON payload matching {@link SleepTelemetryMessage}
     */
    public void onSleepMessage(String jsonPayload) {
        sleepTelemetryConsumer.consume(readPayload(jsonPayload, SleepTelemetryMessage.class));
    }

    private <T> T readPayload(String jsonPayload, Class<T> type) {
        try {
            return objectMapper.readValue(jsonPayload, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("wearable.telemetry.payload.invalid", e);
        }
    }
}
