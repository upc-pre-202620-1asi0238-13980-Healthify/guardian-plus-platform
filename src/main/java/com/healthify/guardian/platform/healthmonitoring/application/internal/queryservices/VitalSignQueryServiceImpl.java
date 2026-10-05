package com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices;

import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetLiveVitalSignsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetVitalSignsByCareRecipientProfileIdAndPeriodQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that resolves vital sign read queries.
 */
@Service
public class VitalSignQueryServiceImpl implements VitalSignQueryService {

    private final VitalSignRepository vitalSignRepository;
    private final VitalSignTypeRepository vitalSignTypeRepository;

    public VitalSignQueryServiceImpl(VitalSignRepository vitalSignRepository,
                                     VitalSignTypeRepository vitalSignTypeRepository) {
        this.vitalSignRepository = vitalSignRepository;
        this.vitalSignTypeRepository = vitalSignTypeRepository;
    }

    @Override
    public List<VitalSign> handle(GetLiveVitalSignsByCareRecipientProfileIdQuery query) {
        return vitalSignTypeRepository.findAll().stream()
                .map(type -> vitalSignRepository.findLatestByCareRecipientProfileIdAndVitalSignTypeId(
                        query.careRecipientProfileId(), type.getId()))
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public List<VitalSign> handle(GetVitalSignsByCareRecipientProfileIdAndPeriodQuery query) {
        return vitalSignRepository.findByCareRecipientProfileIdAndPeriod(query.careRecipientProfileId(), query.dateRange());
    }

    @Override
    public Optional<VitalSign> findById(VitalSignId vitalSignId) {
        return vitalSignRepository.findById(vitalSignId);
    }
}
