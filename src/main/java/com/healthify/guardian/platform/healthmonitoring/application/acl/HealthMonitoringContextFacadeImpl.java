package com.healthify.guardian.platform.healthmonitoring.application.acl;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import com.healthify.guardian.platform.healthmonitoring.interfaces.acl.HealthMonitoringContextFacade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the public facade of the Health Monitoring bounded context.
 */
@Service
public class HealthMonitoringContextFacadeImpl implements HealthMonitoringContextFacade {

    private final WearableDeviceRepository wearableDeviceRepository;
    private final VitalSignRepository vitalSignRepository;
    private final VitalSignTypeRepository vitalSignTypeRepository;

    public HealthMonitoringContextFacadeImpl(WearableDeviceRepository wearableDeviceRepository,
                                             VitalSignRepository vitalSignRepository,
                                             VitalSignTypeRepository vitalSignTypeRepository) {
        this.wearableDeviceRepository = wearableDeviceRepository;
        this.vitalSignRepository = vitalSignRepository;
        this.vitalSignTypeRepository = vitalSignTypeRepository;
    }

    @Override
    public boolean hasAssignedWearableDevice(UUID careRecipientProfileId) {
        return wearableDeviceRepository.findByCareRecipientProfileId(new CareRecipientProfileId(careRecipientProfileId))
                .stream().anyMatch(device -> device.isAssigned());
    }

    @Override
    public Optional<BigDecimal> fetchLatestVitalSignValue(UUID careRecipientProfileId, String vitalSignTypeCode) {
        return vitalSignTypeRepository.findByCode(new VitalSignTypeCode(vitalSignTypeCode))
                .flatMap(type -> vitalSignRepository.findLatestByCareRecipientProfileIdAndVitalSignTypeId(
                        new CareRecipientProfileId(careRecipientProfileId), type.getId()))
                .map(vitalSign -> vitalSign.getValue().value());
    }
}
