package com.healthify.guardian.platform.healthmonitoring.application.queryservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetWearableDevicesByCareRecipientProfileIdQuery;

import java.util.List;

/**
 * Application service contract for wearable device read queries.
 */
public interface WearableDeviceQueryService {

    List<WearableDevice> handle(GetWearableDevicesByCareRecipientProfileIdQuery query);
}
