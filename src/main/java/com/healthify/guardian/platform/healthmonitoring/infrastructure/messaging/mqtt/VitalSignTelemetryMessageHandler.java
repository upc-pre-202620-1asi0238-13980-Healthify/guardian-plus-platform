package com.healthify.guardian.platform.healthmonitoring.infrastructure.messaging.mqtt;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

/**
 * Turns a raw telemetry message received from the broker into a Detect Vital Signs command. It is independent
 * of the MQTT client so it can be tested without a broker.
 *
 * <p>It never throws: a malformed message or a rejected reading is logged and dropped, because an exception
 * would make the MQTT client close the connection and lose the rest of the stream.</p>
 */
@Slf4j
@Component
public class VitalSignTelemetryMessageHandler {

    // Private mapper: the wearable's wire format must not depend on the REST API's Jackson customizations
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final VitalSignCommandService vitalSignCommandService;

    public VitalSignTelemetryMessageHandler(VitalSignCommandService vitalSignCommandService) {
        this.vitalSignCommandService = vitalSignCommandService;
    }

    /**
     * @param topic      topic the message arrived on, only used for logging
     * @param payload    raw JSON payload
     * @param receivedAt server time at which the message arrived
     * @return the result of the Detect command, or empty when the message is not a valid vital sign reading
     */
    public Optional<Result<VitalSign, ApplicationError>> handle(String topic, byte[] payload, Instant receivedAt) {
        try {
            var message = JSON.readValue(new String(payload, StandardCharsets.UTF_8), VitalSignTelemetryMessage.class);
            if (!message.isVitalSignReading()) {
                log.debug("Ignored {} on {}: not a vital sign reading", message.signalType(), topic);
                return Optional.empty();
            }
            var command = DetectVitalSignsCommandFromTelemetryMessageAssembler.toCommandFromMessage(message, receivedAt);
            var result = vitalSignCommandService.handle(command);
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Reading on {} rejected: {}", topic, failure.error());
            }
            return Optional.of(result);
        } catch (JacksonException | IllegalArgumentException e) {
            log.warn("Malformed telemetry message on {}: {}", topic, e.getMessage());
            return Optional.empty();
        } catch (RuntimeException e) {
            log.error("Telemetry message on {} could not be processed", topic, e);
            return Optional.empty();
        }
    }
}
