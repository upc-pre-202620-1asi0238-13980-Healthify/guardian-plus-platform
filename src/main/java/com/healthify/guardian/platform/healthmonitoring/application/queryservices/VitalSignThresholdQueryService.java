package com.healthify.guardian.platform.healthmonitoring.application.queryservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery;

import java.util.List;

/**
 * Application service contract for vital sign threshold read queries.
 */
public interface VitalSignThresholdQueryService {

    List<VitalSignThreshold> handle(GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery query);
}
