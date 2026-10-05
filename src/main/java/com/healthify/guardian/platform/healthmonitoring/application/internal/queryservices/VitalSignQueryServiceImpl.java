package com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices;

import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetLiveVitalSignsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetVitalSignsByCareRecipientProfileIdAndPeriodQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Application service that resolves vital sign read queries.
 */
@Service
public class VitalSignQueryServiceImpl implements VitalSignQueryService {

    private final VitalSignRepository vitalSignRepository;

    public VitalSignQueryServiceImpl(VitalSignRepository vitalSignRepository) {
        this.vitalSignRepository = vitalSignRepository;
    }

    @Override
    public List<VitalSign> handle(GetLiveVitalSignsByCareRecipientProfileIdQuery query) {
        return Arrays.stream(VitalSignType.values())
                .map(type -> vitalSignRepository.findLatestByCareRecipientProfileIdAndVitalSignType(
                        query.careRecipientProfileId(), type))
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
