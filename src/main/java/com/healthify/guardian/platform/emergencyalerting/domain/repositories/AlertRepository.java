package com.healthify.guardian.platform.emergencyalerting.domain.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSource;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Alert aggregate repository port.
 */
public interface AlertRepository {

    /**
     * Persists an alert (create or update), including its deliveries and responses, and publishes
     * its registered domain events.
     *
     * @param alert the alert to save
     * @return the saved alert
     */
    Alert save(Alert alert);

    Optional<Alert> findById(AlertId id);

    /**
     * Retrieves the alert of a Fragile Citizen that is still active for the given source, used to
     * avoid raising the same alert twice when a signal is retried.
     *
     * @param careRecipientProfileId the Fragile Citizen the signal belongs to
     * @param source                 the signal's source
     * @return the active alert for that source, if any
     */
    Optional<Alert> findActiveBySource(CareRecipientProfileId careRecipientProfileId, AlertSource source);

    /**
     * Retrieves the alerts of a Fragile Citizen that are neither dismissed nor resolved.
     *
     * @param careRecipientProfileId the Fragile Citizen whose active alerts are requested
     * @return the active alerts, most recent first
     */
    List<Alert> findActiveByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId);

    /**
     * Pages through a Fragile Citizen's alert history.
     *
     * @param careRecipientProfileId the Fragile Citizen whose history is requested
     * @param period                 the triggering period to filter by
     * @param severity               the severity to filter by, or {@code null} for every severity
     * @param pageable               the page to retrieve
     * @return the requested page, most recent first
     */
    Page<Alert> findHistory(
            CareRecipientProfileId careRecipientProfileId, DateRange period, Severity severity, Pageable pageable);

    /**
     * Retrieves the dispatched alerts the given user was notified about and that nobody has
     * acknowledged yet.
     *
     * @param recipientUserId the recipient whose pending alerts are requested
     * @return the pending alerts, most recent first
     */
    List<Alert> findPendingByRecipientUserId(UserId recipientUserId);

    /**
     * Retrieves every alert still pending confirmation that was triggered before the given instant,
     * for {@code FallConfirmationTimeoutScheduler} to confirm.
     *
     * @param threshold the latest triggering time whose confirmation window has already elapsed
     * @return the alerts due for confirmation
     */
    List<Alert> findPendingConfirmationTriggeredBefore(Instant threshold);

    /**
     * Retrieves every dispatched alert still awaiting acknowledgement, for
     * {@code AckTimeoutEscalationScheduler} to evaluate.
     *
     * @return the alerts awaiting acknowledgement
     */
    List<Alert> findAwaitingAcknowledgement();
}
