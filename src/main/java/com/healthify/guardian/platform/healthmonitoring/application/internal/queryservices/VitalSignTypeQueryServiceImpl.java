package com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices;

import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignTypeQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetAllVitalSignTypesQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that resolves vital sign type catalog read queries.
 */
@Service
public class VitalSignTypeQueryServiceImpl implements VitalSignTypeQueryService {

    private final VitalSignTypeRepository vitalSignTypeRepository;

    public VitalSignTypeQueryServiceImpl(VitalSignTypeRepository vitalSignTypeRepository) {
        this.vitalSignTypeRepository = vitalSignTypeRepository;
    }

    @Override
    public List<VitalSignType> handle(GetAllVitalSignTypesQuery query) {
        return vitalSignTypeRepository.findAll();
    }
}
