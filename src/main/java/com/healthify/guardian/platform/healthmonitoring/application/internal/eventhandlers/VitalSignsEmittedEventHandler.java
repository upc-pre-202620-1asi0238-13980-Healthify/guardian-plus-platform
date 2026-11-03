package com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EvaluateVitalSignsThresholdsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsEmittedEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to {@link VitalSignsEmittedEvent} by evaluating the reading against the normal range of its type (Emit -> Evaluate).
 *
 * <p>Never lets a failure propagate back to the publisher: it is logged and the reading stays stored.</p>
 */
@Slf4j
@Service
public class VitalSignsEmittedEventHandler {

    private final VitalSignCommandService vitalSignCommandService;

    public VitalSignsEmittedEventHandler(VitalSignCommandService vitalSignCommandService) {
        this.vitalSignCommandService = vitalSignCommandService;
    }

    @EventListener
    public void on(VitalSignsEmittedEvent event) {
        try {
            var result = vitalSignCommandService.handle(new EvaluateVitalSignsThresholdsCommand(event.vitalSignId().value()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("threshold evaluation of vital sign {} failed: {}", event.vitalSignId().value(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure in threshold evaluation of vital sign {}", event.vitalSignId().value(), e);
        }
    }
}
