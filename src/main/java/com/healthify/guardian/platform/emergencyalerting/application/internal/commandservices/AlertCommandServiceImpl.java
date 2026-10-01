package com.healthify.guardian.platform.emergencyalerting.application.internal.commandservices;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.AcknowledgeAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.BroadcastAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ClaimAlertResponseCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.CompleteAlertResponseCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ConfirmAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DismissAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DispatchAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.EscalateAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.RegisterDeliveryResultCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ResolveAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertDeliveryId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertResponseId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSource;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryTarget;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.RecipientLevel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertChannelSettingRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertSettingsRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.EmergencyContactRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.services.DispatchStrategyPolicy;
import com.healthify.guardian.platform.emergencyalerting.domain.services.EscalationPolicy;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Application service that executes alert commands.
 *
 * <p>Leaves every business decision to the {@code Alert} aggregate and to
 * {@code DispatchStrategyPolicy} / {@code EscalationPolicy}; this service only loads what they need
 * (settings, active contacts and their channels), hands them the current time and persists the
 * outcome.</p>
 */
@Service
public class AlertCommandServiceImpl implements AlertCommandService {

    private static final String CONCURRENT_MODIFICATION_MESSAGE_KEY = "alert.concurrent-modification";
    private static final int MAX_ATTEMPTS = 5;

    private final AlertRepository alertRepository;
    private final AlertSettingsRepository alertSettingsRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final AlertChannelSettingRepository alertChannelSettingRepository;
    private final DispatchStrategyPolicy dispatchStrategyPolicy;
    private final EscalationPolicy escalationPolicy;
    private final Clock clock;

    public AlertCommandServiceImpl(
            AlertRepository alertRepository,
            AlertSettingsRepository alertSettingsRepository,
            EmergencyContactRepository emergencyContactRepository,
            AlertChannelSettingRepository alertChannelSettingRepository,
            DispatchStrategyPolicy dispatchStrategyPolicy,
            EscalationPolicy escalationPolicy,
            Clock emergencyAlertingClock) {
        this.alertRepository = alertRepository;
        this.alertSettingsRepository = alertSettingsRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.alertChannelSettingRepository = alertChannelSettingRepository;
        this.dispatchStrategyPolicy = dispatchStrategyPolicy;
        this.escalationPolicy = escalationPolicy;
        this.clock = emergencyAlertingClock;
    }

    @Override
    public Result<Alert, ApplicationError> handle(TriggerAlertCommand command) {
        try {
            var careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
            var source = new AlertSource(command.sourceType(), command.sourceReferenceId());
            var alreadyActive = alertRepository.findActiveBySource(careRecipientProfileId, source);
            if (alreadyActive.isPresent()) {
                return Result.success(alreadyActive.get());
            }
            return Result.success(saveAndReload(new Alert(command)));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("trigger-alert", resolve(e)));
        }
    }

    @Override
    public Result<Alert, ApplicationError> handle(ConfirmAlertCommand command) {
        return applyToExistingAlert(command.alertId(), "confirm-alert", Alert::confirm);
    }

    @Override
    public Result<Alert, ApplicationError> handle(DismissAlertCommand command) {
        return applyToExistingAlert(command.alertId(), "dismiss-alert", Alert::dismissAsFalsePositive);
    }

    @Override
    public Result<Alert, ApplicationError> handle(DispatchAlertCommand command) {
        return applyToExistingAlert(command.alertId(), "dispatch-alert", (alert, now) -> {
            var settings = settingsFor(alert);
            var level = dispatchStrategyPolicy.resolveInitialLevel(alert.getSeverity(), settings);
            alert.dispatch(level, resolveTargets(alert, level), now);
        });
    }

    @Override
    public Result<Alert, ApplicationError> handle(EscalateAlertCommand command) {
        return applyToExistingAlert(command.alertId(), "escalate-alert", (alert, now) ->
                alert.escalate(resolveTargets(alert, RecipientLevel.SECONDARY), now));
    }

    @Override
    public Result<Alert, ApplicationError> handle(BroadcastAlertCommand command) {
        return applyToExistingAlert(command.alertId(), "broadcast-alert", (alert, now) ->
                alert.broadcast(resolveTargets(alert, RecipientLevel.BROADCAST), now));
    }

    @Override
    public Result<Alert, ApplicationError> handle(RegisterDeliveryResultCommand command) {
        return applyToExistingAlert(command.alertId(), "register-delivery-result", (alert, now) ->
                alert.registerDeliveryResult(
                        new AlertDeliveryId(command.deliveryId()),
                        command.deliveryStatus(),
                        command.occurredAt() != null ? command.occurredAt() : now));
    }

    @Override
    public Result<Alert, ApplicationError> handle(AcknowledgeAlertCommand command) {
        return applyToExistingAlert(command.alertId(), "acknowledge-alert", (alert, now) ->
                alert.acknowledge(new UserId(command.userId()), now));
    }

    @Override
    public Result<Alert, ApplicationError> handle(ClaimAlertResponseCommand command) {
        return applyToExistingAlert(command.alertId(), "claim-alert-response", (alert, now) ->
                alert.claimResponse(new UserId(command.responderUserId()), now));
    }

    @Override
    public Result<Alert, ApplicationError> handle(CompleteAlertResponseCommand command) {
        return applyToExistingAlert(command.alertId(), "complete-alert-response", (alert, now) ->
                alert.completeResponse(new AlertResponseId(command.responseId()), command.notes(), now));
    }

    @Override
    public Result<Alert, ApplicationError> handle(ResolveAlertCommand command) {
        return applyToExistingAlert(command.alertId(), "resolve-alert", Alert::resolve);
    }

    /**
     * Resolves the delivery targets of one escalation level from the Fragile Citizen's active
     * contacts and those contacts' channel settings.
     */
    private List<DeliveryTarget> resolveTargets(Alert alert, RecipientLevel level) {
        var contacts = emergencyContactRepository
                .findActiveByCareRecipientProfileIdOrderByPriority(alert.getCareRecipientProfileId());
        var channelSettings = alertChannelSettingRepository.findByUserIdIn(
                contacts.stream().map(EmergencyContact::getUserId).toList());
        return escalationPolicy.resolveRecipients(level, alert.getSeverity(), contacts, channelSettings);
    }

    /** Returns the Fragile Citizen's settings, or the defaults if they never changed them. */
    private AlertSettings settingsFor(Alert alert) {
        return alertSettingsRepository.findByCareRecipientProfileId(alert.getCareRecipientProfileId())
                .orElseGet(() -> new AlertSettings(alert.getCareRecipientProfileId()));
    }

    /**
     * Loads the alert, applies the transition and saves it. When the save loses an optimistic-lock
     * race (e.g. a recipient acknowledges while a delivery outcome is being recorded), the alert is
     * reloaded and the transition re-evaluated against the fresh state, so the business rules
     * decide the outcome rather than the timing; only a persistent conflict reaches the caller.
     */
    private Result<Alert, ApplicationError> applyToExistingAlert(
            UUID alertId, String operation, BiConsumer<Alert, Instant> transition) {
        if (alertId == null) {
            return Result.failure(ApplicationError.validationError(operation, resolve("alert.id.invalid")));
        }

        for (int attempt = 1; ; attempt++) {
            var alert = alertRepository.findById(new AlertId(alertId));
            if (alert.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Alert", alertId.toString()));
            }
            try {
                transition.accept(alert.get(), clock.instant());
                return Result.success(saveAndReload(alert.get()));
            } catch (IllegalArgumentException e) {
                return Result.failure(ApplicationError.validationError(operation, resolve(e)));
            } catch (IllegalStateException e) {
                return Result.failure(ApplicationError.businessRuleViolation(operation, resolve(e)));
            } catch (OptimisticLockingFailureException e) {
                if (attempt == MAX_ATTEMPTS) {
                    return Result.failure(ApplicationError.conflict("Alert", resolve(CONCURRENT_MODIFICATION_MESSAGE_KEY)));
                }
            }
        }
    }

    /**
     * Saves the alert and returns its latest state. Saving publishes the alert's events, and their
     * handlers may synchronously move it further (e.g. a triggered SOS is confirmed and dispatched
     * before {@code save} returns), so the snapshot taken at save time would already be stale.
     */
    private Alert saveAndReload(Alert alert) {
        var saved = alertRepository.save(alert);
        return alertRepository.findById(saved.getId()).orElse(saved);
    }

    /** Resolves a domain exception whose message is a bundle key into the localized sentence. */
    private static String resolve(RuntimeException e) {
        return resolve(e.getMessage());
    }

    private static String resolve(String messageKey) {
        return MessageResolver.resolveOrDefault(messageKey, messageKey);
    }
}
