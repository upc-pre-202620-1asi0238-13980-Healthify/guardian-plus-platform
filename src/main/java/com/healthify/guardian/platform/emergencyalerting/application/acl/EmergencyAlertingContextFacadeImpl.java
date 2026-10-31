package com.healthify.guardian.platform.emergencyalerting.application.acl;

import com.healthify.guardian.platform.emergencyalerting.application.queryservices.AlertQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetActiveAlertsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.interfaces.acl.EmergencyAlertingContextFacade;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implements the public facade of this bounded context on top of its query services.
 */
@Service
public class EmergencyAlertingContextFacadeImpl implements EmergencyAlertingContextFacade {

    private final AlertQueryService alertQueryService;

    public EmergencyAlertingContextFacadeImpl(AlertQueryService alertQueryService) {
        this.alertQueryService = alertQueryService;
    }

    @Override
    public boolean hasActiveAlerts(UUID careRecipientProfileId) {
        if (careRecipientProfileId == null) {
            return false;
        }
        return !alertQueryService.handle(new GetActiveAlertsByCareRecipientProfileIdQuery(
                new CareRecipientProfileId(careRecipientProfileId))).isEmpty();
    }
}
