package com.healthify.guardian.platform.emergencyalerting.application.queryservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetActiveAlertsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertByIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertHistoryByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetPendingAlertsByRecipientUserIdQuery;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for queries over the {@code Alert} aggregate.
 */
public interface AlertQueryService {

    Optional<Alert> handle(GetAlertByIdQuery query);

    /**
     * @return the Fragile Citizen's alerts that are neither dismissed nor resolved, most recent first
     */
    List<Alert> handle(GetActiveAlertsByCareRecipientProfileIdQuery query);

    /**
     * @return the requested page of the Fragile Citizen's alert history, most recent first
     */
    Page<Alert> handle(GetAlertHistoryByCareRecipientProfileIdQuery query);

    /**
     * @return the alerts the recipient was notified about and nobody has acknowledged yet, most recent first
     */
    List<Alert> handle(GetPendingAlertsByRecipientUserIdQuery query);
}
