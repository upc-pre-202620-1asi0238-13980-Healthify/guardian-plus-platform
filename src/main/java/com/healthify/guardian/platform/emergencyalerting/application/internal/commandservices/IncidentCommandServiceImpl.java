package com.healthify.guardian.platform.emergencyalerting.application.internal.commandservices;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.IncidentCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.CloseIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.OpenIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.StabilizeIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.IncidentRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Application service that executes incident commands.
 */
@Service
public class IncidentCommandServiceImpl implements IncidentCommandService {

    private static final String ALREADY_OPENED_MESSAGE_KEY = "incident.already-opened";
    private static final String ALERT_NOT_ACKNOWLEDGED_MESSAGE_KEY = "incident.alert-not-acknowledged";

    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final Clock clock;

    public IncidentCommandServiceImpl(
            IncidentRepository incidentRepository, AlertRepository alertRepository, Clock emergencyAlertingClock) {
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.clock = emergencyAlertingClock;
    }

    @Override
    public Result<Incident, ApplicationError> handle(OpenIncidentCommand command) {
        if (command.alertId() == null) {
            return Result.failure(ApplicationError.validationError("open-incident", resolve("alert.id.invalid")));
        }
        var alertId = new AlertId(command.alertId());
        var alert = alertRepository.findById(alertId);
        if (alert.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Alert", command.alertId().toString()));
        }
        if (alert.get().getStatus() != AlertStatus.ACKNOWLEDGED) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "open-incident", resolve(ALERT_NOT_ACKNOWLEDGED_MESSAGE_KEY)));
        }
        if (incidentRepository.existsByAlertId(alertId)) {
            return Result.failure(ApplicationError.conflict("Incident", resolve(ALREADY_OPENED_MESSAGE_KEY)));
        }

        try {
            return Result.success(incidentRepository.save(new Incident(command)));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("open-incident", resolve(e.getMessage())));
        } catch (DataIntegrityViolationException e) {
            // Another request opened it between the existence check and the insert.
            return Result.failure(ApplicationError.conflict("Incident", resolve(ALREADY_OPENED_MESSAGE_KEY)));
        }
    }

    @Override
    public Result<Incident, ApplicationError> handle(StabilizeIncidentCommand command) {
        return applyToExistingIncident(command.incidentId(), "stabilize-incident",
                (incident, now) -> incident.stabilize(command.notes(), now));
    }

    @Override
    public Result<Incident, ApplicationError> handle(CloseIncidentCommand command) {
        return applyToExistingIncident(command.incidentId(), "close-incident",
                (incident, now) -> incident.close(command.notes(), now));
    }

    private Result<Incident, ApplicationError> applyToExistingIncident(
            UUID incidentId, String operation, BiConsumer<Incident, Instant> transition) {
        if (incidentId == null) {
            return Result.failure(ApplicationError.validationError(operation, resolve("incident.id.invalid")));
        }
        var incident = incidentRepository.findById(new IncidentId(incidentId));
        if (incident.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Incident", incidentId.toString()));
        }

        try {
            transition.accept(incident.get(), clock.instant());
            return Result.success(incidentRepository.save(incident.get()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation(operation, resolve(e.getMessage())));
        }
    }

    private static String resolve(String messageKey) {
        return MessageResolver.resolveOrDefault(messageKey, messageKey);
    }
}
