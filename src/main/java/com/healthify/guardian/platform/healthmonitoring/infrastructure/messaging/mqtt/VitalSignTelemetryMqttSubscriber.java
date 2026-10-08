package com.healthify.guardian.platform.healthmonitoring.infrastructure.messaging.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Connects Health Monitoring to the wearable's MQTT broker over WebSocket ({@code ws://} or {@code wss://})
 * and subscribes to the vital sign channel ({@code guardian/vitals/+}) published by the wearable or the IoT
 * simulator. Every message is handed to {@link VitalSignTelemetryMessageHandler}.
 *
 * <p>Only created when {@code health-monitoring.telemetry.mqtt.enabled=true}. The connection is made in the
 * background: if the broker is down the application still starts and keeps retrying, and once connected the
 * client reconnects and resubscribes on its own.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "health-monitoring.telemetry.mqtt.enabled", havingValue = "true")
public class VitalSignTelemetryMqttSubscriber implements SmartLifecycle, MqttCallbackExtended {

    private final VitalSignTelemetryMessageHandler handler;
    private final String brokerUrl;
    private final String clientId;
    private final String topic;
    private final int qos;
    private final long reconnectDelaySeconds;

    private final ScheduledExecutorService connector = Executors.newSingleThreadScheduledExecutor(runnable -> {
        var thread = new Thread(runnable, "health-monitoring-mqtt-connector");
        thread.setDaemon(true);
        return thread;
    });
    private MqttClient client;
    private volatile boolean running;

    public VitalSignTelemetryMqttSubscriber(
            VitalSignTelemetryMessageHandler handler,
            @Value("${health-monitoring.telemetry.mqtt.broker-url:ws://localhost:9001}") String brokerUrl,
            @Value("${health-monitoring.telemetry.mqtt.client-id:guardian-plus-health-monitoring}") String clientId,
            @Value("${health-monitoring.telemetry.mqtt.topic:guardian/vitals/+}") String topic,
            @Value("${health-monitoring.telemetry.mqtt.qos:1}") int qos,
            @Value("${health-monitoring.telemetry.mqtt.reconnect-delay-seconds:5}") long reconnectDelaySeconds) {
        this.handler = handler;
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.reconnectDelaySeconds = reconnectDelaySeconds;
    }

    @Override
    public void start() {
        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        } catch (MqttException e) {
            throw new IllegalStateException("Invalid MQTT broker URL " + brokerUrl, e);
        }
        client.setCallback(this);
        running = true;
        connector.execute(this::connect);
    }

    private void connect() {
        if (!running) {
            return;
        }
        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setKeepAliveInterval(30);
        try {
            client.connect(options);
        } catch (MqttException e) {
            // automatic reconnect only kicks in after a first successful connection
            log.warn("MQTT broker {} not reachable ({}), retrying in {}s", brokerUrl, e.getMessage(), reconnectDelaySeconds);
            connector.schedule(this::connect, reconnectDelaySeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        // a clean session forgets the subscriptions, so they are renewed on every (re)connection
        try {
            client.subscribe(topic, qos);
            log.info("{} to MQTT broker {}, subscribed to {}", reconnect ? "Reconnected" : "Connected", serverUri, topic);
        } catch (MqttException e) {
            log.error("Could not subscribe to {} on {}", topic, serverUri, e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Connection to MQTT broker {} lost: {}", brokerUrl, cause == null ? "unknown" : cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        handler.handle(topic, message.getPayload(), Instant.now());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // subscriber only, nothing is published
    }

    @Override
    public void stop() {
        running = false;
        connector.shutdownNow();
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        } catch (MqttException e) {
            log.warn("Error closing the MQTT connection to {}", brokerUrl, e);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
