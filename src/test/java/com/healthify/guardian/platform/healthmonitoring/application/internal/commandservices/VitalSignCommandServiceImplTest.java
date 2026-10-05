package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.LinkWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
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
    @Mock WearableDeviceRepository wearableDeviceRepository;

    private VitalSignCommandServiceImpl service;
    private WearableDevice device;

    @BeforeEach
    void setUp() {
        service = new VitalSignCommandServiceImpl(vitalSignRepository, wearableDeviceRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
        device = new WearableDevice(new LinkWearableDeviceCommand(RECIPIENT, "GP-0001", "WRISTBAND"));
    }

    private DetectVitalSignsCommand command(UUID recipient, String type, String value) {
        return new DetectVitalSignsCommand(device.getId().value(), recipient, type,
                new BigDecimal(value), NOW.minusSeconds(2), NOW);
    }

    private DetectVitalSignsCommand command(UUID recipient) {
        return command(recipient, "HR", "72");
    }

    @Test
    void storesReadingOfALinkedDevice() {
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.of(device));
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
    void rejectsUnknownVitalSignType() {
        var result = service.handle(command(RECIPIENT, "GLUCOSE", "90"));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");
        verify(vitalSignRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateReading() {
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.of(device));
        when(vitalSignRepository.existsByWearableDeviceIdAndVitalSignTypeAndMeasuredAt(any(), any(), any())).thenReturn(true);

        var result = service.handle(command(RECIPIENT));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VITALSIGN_CONFLICT");
    }

    @Test
    void rejectsReadingOutsideThePhysicalLimitsOfItsType() {
        when(wearableDeviceRepository.findById(device.getId())).thenReturn(Optional.of(device));
        var result = service.handle(command(RECIPIENT, "HR", "400"));

        assertThat(((Result.Failure<?, ?>) result).error()).hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");
        verify(vitalSignRepository, never()).save(any(VitalSign.class));
    }
}
