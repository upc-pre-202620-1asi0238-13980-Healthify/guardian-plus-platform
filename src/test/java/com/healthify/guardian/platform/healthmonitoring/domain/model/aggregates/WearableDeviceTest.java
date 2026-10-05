package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.AssignWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DeviceStatus;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DeviceType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WearableDeviceTest {

    private static final UUID RECIPIENT = UUID.randomUUID();

    @Test
    void isAssignedOnCreation() {
        var device = new WearableDevice(new AssignWearableDeviceCommand(RECIPIENT, " GP-0001 ", "wristband"));

        assertThat(device.getStatus()).isEqualTo(DeviceStatus.ASSIGNED);
        assertThat(device.getDeviceType()).isEqualTo(DeviceType.WRISTBAND);
        assertThat(device.getSerialNumber().value()).isEqualTo("GP-0001");
        assertThat(device.getAssignedAt()).isNotNull();
    }

    @Test
    void rejectsUnknownDeviceType() {
        assertThatThrownBy(() -> new WearableDevice(new AssignWearableDeviceCommand(RECIPIENT, "GP-0001", "PHONE")))
                .hasMessage("wearable-device.device-type.invalid");
    }

    @Test
    void onlyReportsForItsOwnCareRecipientWhileAssigned() {
        var device = new WearableDevice(new AssignWearableDeviceCommand(RECIPIENT, "GP-0001", "SMARTWATCH"));

        assertThat(device.canReportFor(new CareRecipientProfileId(RECIPIENT))).isTrue();
        assertThat(device.canReportFor(new CareRecipientProfileId(UUID.randomUUID()))).isFalse();

        device.deactivate();

        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE);
        assertThat(device.canReportFor(new CareRecipientProfileId(RECIPIENT))).isFalse();
    }

    @Test
    void cannotBeDeactivatedTwice() {
        var device = new WearableDevice(new AssignWearableDeviceCommand(RECIPIENT, "GP-0001", "PATCH"));
        device.deactivate();

        assertThatThrownBy(device::deactivate).hasMessage("wearable-device.cannot.deactivate");
    }
}
