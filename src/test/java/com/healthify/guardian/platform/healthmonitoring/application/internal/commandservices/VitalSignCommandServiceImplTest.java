package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.AssignWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignThresholdRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests of {@link VitalSignCommandServiceImpl} in isolation: repositories are mocked.
 */
@ExtendWith(MockitoExtension.class)
class VitalSignCommandServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-05T15:00:00Z");
    private static final UUID RECIPIENT = UUID.randomUUID();

    @Mock VitalSignRepository vitalSignRepository;
    @Mock VitalSignThresholdRepository vitalSignThresholdRepository;
    @Mock WearableDeviceRepository wearableDeviceRepository;
    @Mock VitalSignTypeRepository vitalSignTypeRepository;

    private VitalSignCommandServiceImpl service;
    private WearableDevice device;
    private VitalSignType heartRate;

    @BeforeEach
    void setUp() {
        service = new VitalSignCommandServiceImpl(vitalSignRepository, vitalSignThresholdRepository,
                wearableDeviceRepository, vitalSignTypeRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        device = new WearableDevice(new AssignWearableDeviceCommand(RECIPIENT, "GP-0001", "WRISTBAND"));
        heartRate = new VitalSignType(new RegisterVitalSignTypeCommand("HR", "Heart rate", "bpm"));
    }

    private DetectVitalSignsCommand command(UUID recipient) {
        return new DetectVitalSignsCommand(device.getId().value(), recipient, heartRate.getId().value(),
                new BigDecimal("72"), NOW.minusSeconds(2), NOW);
    }

    @Test
    void storesReadingOfAnAssignedDevice() {
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.of(device));
        when(vitalSignTypeRepository.findById(heartRate.getId())).thenReturn(Optional.of(heartRate));
        when(vitalSignRepository.save(any(VitalSign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(command(RECIPIENT));

        assertThat(result.isSuccess()).isTrue();
        verify(vitalSignRepository).save(any(VitalSign.class));
    }

    @Test
    void failsWhenDeviceDoesNotExist() {
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.empty());

        var result = service.handle(command(RECIPIENT));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "WEARABLEDEVICE_NOT_FOUND");
        verify(vitalSignRepository, never()).save(any());
    }

    @Test
    void rejectsReadingReportedForAnotherCareRecipient() {
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.of(device));

        var result = service.handle(command(UUID.randomUUID()));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "BUSINESS_RULE_VIOLATION");
    }

    @Test
    void failsWhenVitalSignTypeDoesNotExist() {
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.of(device));
        when(vitalSignTypeRepository.findById(heartRate.getId())).thenReturn(Optional.empty());

        var result = service.handle(command(RECIPIENT));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VITALSIGNTYPE_NOT_FOUND");
    }

    @Test
    void rejectsDuplicateReading() {
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.of(device));
        when(vitalSignTypeRepository.findById(heartRate.getId())).thenReturn(Optional.of(heartRate));
        when(vitalSignRepository.existsByWearableDeviceIdAndVitalSignTypeIdAndMeasuredAt(any(), any(), any())).thenReturn(true);

        var result = service.handle(command(RECIPIENT));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VITALSIGN_CONFLICT");
    }
}
