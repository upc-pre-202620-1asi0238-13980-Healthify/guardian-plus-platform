package com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices;

import com.healthify.guardian.platform.healthmonitoring.application.queryservices.WearableDeviceQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetAllWearableDevicesQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetWearableDevicesByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that resolves wearable device read queries.
 */
@Service
public class WearableDeviceQueryServiceImpl implements WearableDeviceQueryService {

    private final WearableDeviceRepository wearableDeviceRepository;

    public WearableDeviceQueryServiceImpl(WearableDeviceRepository wearableDeviceRepository) {
        this.wearableDeviceRepository = wearableDeviceRepository;
    }

    @Override
    public List<WearableDevice> handle(GetWearableDevicesByCareRecipientProfileIdQuery query) {
        return wearableDeviceRepository.findByCareRecipientProfileId(query.careRecipientProfileId());
    }

    @Override
    public List<WearableDevice> handle(GetAllWearableDevicesQuery query) {
        return wearableDeviceRepository.findAll();
    }
}
