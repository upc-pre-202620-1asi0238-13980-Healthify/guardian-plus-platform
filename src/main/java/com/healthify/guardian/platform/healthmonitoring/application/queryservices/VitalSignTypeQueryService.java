package com.healthify.guardian.platform.healthmonitoring.application.queryservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetAllVitalSignTypesQuery;

import java.util.List;

/**
 * Application service contract for vital sign type catalog read queries.
 */
public interface VitalSignTypeQueryService {

    List<VitalSignType> handle(GetAllVitalSignTypesQuery query);
}
