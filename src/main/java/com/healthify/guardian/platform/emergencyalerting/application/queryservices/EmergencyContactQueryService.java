package com.healthify.guardian.platform.emergencyalerting.application.queryservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetEmergencyContactsByCareRecipientProfileIdQuery;

import java.util.List;

/**
 * Application service contract for queries over the {@code EmergencyContact} aggregate.
 */
public interface EmergencyContactQueryService {

    /**
     * @return the Fragile Citizen's active emergency contacts, primary contact first
     */
    List<EmergencyContact> handle(GetEmergencyContactsByCareRecipientProfileIdQuery query);
}
