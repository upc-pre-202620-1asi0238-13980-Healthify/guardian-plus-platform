package com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices;

import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignThresholdQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignThresholdRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that resolves vital sign threshold read queries.
 */
@Service
public class VitalSignThresholdQueryServiceImpl implements VitalSignThresholdQueryService {

    private final VitalSignThresholdRepository vitalSignThresholdRepository;

    public VitalSignThresholdQueryServiceImpl(VitalSignThresholdRepository vitalSignThresholdRepository) {
        this.vitalSignThresholdRepository = vitalSignThresholdRepository;
    }

    @Override
    public List<VitalSignThreshold> handle(GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery query) {
        return vitalSignThresholdRepository.findAllActiveByCareRecipientProfileId(query.careRecipientProfileId());
    }
}
