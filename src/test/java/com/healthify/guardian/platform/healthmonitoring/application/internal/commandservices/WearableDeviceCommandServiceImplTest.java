package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands   .LinkWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.UnlinkWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WearableDeviceCommandServiceImplTest {

    @Mock WearableDeviceRepository wearableDeviceRepository;
    @InjectMocks WearableDeviceCommandServiceImpl service;

    @Test
    void linksDeviceWithUnusedSerialNumber() {
        when(wearableDeviceRepository.findBySerialNumber(any())).thenReturn(Optional.empty());
        when(wearableDeviceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new LinkWearableDeviceCommand(UUID.randomUUID(), "GP-0001", "SMARTWATCH"));

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void rejectsDuplicatedSerialNumber() {
        var existing = new WearableDevice(new LinkWearableDeviceCommand(UUID.randomUUID(), "GP-0001", "SMARTWATCH"));
        when(wearableDeviceRepository.findBySerialNumber(existing.getSerialNumber())).thenReturn(Optional.of(existing));

        var result = service.handle(new LinkWearableDeviceCommand(UUID.randomUUID(), "GP-0001", "WRISTBAND"));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "WEARABLEDEVICE_CONFLICT");
    }

    @Test
    void unlinksALinkedDevice() {
        var device = new WearableDevice(new LinkWearableDeviceCommand(UUID.randomUUID(), "GP-0001", "WRISTBAND"));
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.of(device));

        var result = service.handle(new UnlinkWearableDeviceCommand(device.getId()));

        assertThat(result.isSuccess()).isTrue();
        verify(wearableDeviceRepository).delete(device.getId());
    }

    @Test
    void rejectsUnlinkingAnUnknownDevice() {
        when(wearableDeviceRepository.findById(any())).thenReturn(Optional.empty());

        var result = service.handle(new UnlinkWearableDeviceCommand(new WearableDeviceId(UUID.randomUUID())));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "WEARABLEDEVICE_NOT_FOUND");
        verify(wearableDeviceRepository, never()).delete(any());
    }

    @Test
    void rejectsInvalidDeviceTypeAsValidationError() {
        var result = service.handle(new LinkWearableDeviceCommand(UUID.randomUUID(), "GP-0001", "TOASTER"));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");
    }
}
