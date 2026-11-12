package com.healthify.guardian.platform.emergencyalerting.application.internal.queryservices;

import com.healthify.guardian.platform.emergencyalerting.application.queryservices.EmergencyContactQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetEmergencyContactsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.EmergencyContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that answers emergency contact queries.
 */
@Service
public class EmergencyContactQueryServiceImpl implements EmergencyContactQueryService {

    private final EmergencyContactRepository emergencyContactRepository;

    public EmergencyContactQueryServiceImpl(EmergencyContactRepository emergencyContactRepository) {
        this.emergencyContactRepository = emergencyContactRepository;
    }

    @Override
    public List<EmergencyContact> handle(GetEmergencyContactsByCareRecipientProfileIdQuery query) {
        return emergencyContactRepository.findActiveByCareRecipientProfileIdOrderByPriority(query.careRecipientProfileId());
    }
}
