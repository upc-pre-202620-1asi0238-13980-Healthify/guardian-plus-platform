package com.healthify.guardian.platform.healthmonitoring.application.queryservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetLiveVitalSignsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetVitalSignsByCareRecipientProfileIdAndPeriodQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for vital sign read queries.
 */
public interface VitalSignQueryService {

    /**
     * Retrieves the latest emitted reading of every vital sign type of a care recipient.
     */
    List<VitalSign> handle(GetLiveVitalSignsByCareRecipientProfileIdQuery query);

    /**
     * Retrieves the readings of a care recipient within a period, oldest first.
     */
    List<VitalSign> handle(GetVitalSignsByCareRecipientProfileIdAndPeriodQuery query);

    Optional<VitalSign> findById(VitalSignId vitalSignId);
}
