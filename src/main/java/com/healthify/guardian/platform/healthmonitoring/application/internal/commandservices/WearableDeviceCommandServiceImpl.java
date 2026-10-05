package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.WearableDeviceCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.LinkWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

/**
 * Application service that executes wearable device commands.
 */
@Service
public class WearableDeviceCommandServiceImpl implements WearableDeviceCommandService {

    private static final String SERIAL_NUMBER_TAKEN_KEY = "wearable-device.serial-number.taken";

    private final WearableDeviceRepository wearableDeviceRepository;

    public WearableDeviceCommandServiceImpl(WearableDeviceRepository wearableDeviceRepository) {
        this.wearableDeviceRepository = wearableDeviceRepository;
    }

    @Override
    public Result<WearableDevice, ApplicationError> handle(LinkWearableDeviceCommand command) {
        try {
            var device = new WearableDevice(command);
            if (wearableDeviceRepository.findBySerialNumber(device.getSerialNumber()).isPresent()) {
                return Result.failure(ApplicationError.conflict("WearableDevice",
                        MessageResolver.resolveOrDefault(SERIAL_NUMBER_TAKEN_KEY, SERIAL_NUMBER_TAKEN_KEY)));
            }
            return Result.success(wearableDeviceRepository.save(device));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "link-wearable-device", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }
}
