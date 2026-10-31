package com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EmitVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsDetectedEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to {@link VitalSignsDetectedEvent} by emitting the reading for live consumption (Detect -> Emit).
 *
 * <p>Never lets a failure propagate back to the publisher: it is logged and the reading stays stored.</p>
 */
@Slf4j
@Service
public class VitalSignsDetectedEventHandler {

    private final VitalSignCommandService vitalSignCommandService;

    public VitalSignsDetectedEventHandler(VitalSignCommandService vitalSignCommandService) {
        this.vitalSignCommandService = vitalSignCommandService;
    }

    @EventListener
    public void on(VitalSignsDetectedEvent event) {
        try {
            var result = vitalSignCommandService.handle(new EmitVitalSignsCommand(event.vitalSignId().value()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("emission of vital sign {} failed: {}", event.vitalSignId().value(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure in emission of vital sign {}", event.vitalSignId().value(), e);
        }
    }
}
