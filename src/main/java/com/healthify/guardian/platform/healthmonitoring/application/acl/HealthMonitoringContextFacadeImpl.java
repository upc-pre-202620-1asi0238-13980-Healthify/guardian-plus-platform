package com.healthify.guardian.platform.healthmonitoring.application.acl;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
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

    public HealthMonitoringContextFacadeImpl(WearableDeviceRepository wearableDeviceRepository,
                                             VitalSignRepository vitalSignRepository) {
        this.wearableDeviceRepository = wearableDeviceRepository;
        this.vitalSignRepository = vitalSignRepository;
    }

    @Override
    public boolean hasLinkedWearableDevice(UUID careRecipientProfileId) {
        return !wearableDeviceRepository.findByCareRecipientProfileId(new CareRecipientProfileId(careRecipientProfileId)).isEmpty();
    }

    @Override
    public Optional<BigDecimal> fetchLatestVitalSignValue(UUID careRecipientProfileId, String vitalSignTypeCode) {
        VitalSignType type;
        try {
            type = VitalSignType.fromCode(vitalSignTypeCode);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return vitalSignRepository.findLatestByCareRecipientProfileIdAndVitalSignType(
                        new CareRecipientProfileId(careRecipientProfileId), type)
                .map(vitalSign -> vitalSign.getValue().value());
    }
}
