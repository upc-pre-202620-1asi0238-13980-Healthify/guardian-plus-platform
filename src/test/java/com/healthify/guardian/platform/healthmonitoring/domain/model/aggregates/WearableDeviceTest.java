package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.LinkWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.WearableDeviceLinkedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DeviceType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WearableDeviceTest {

    private static final UUID RECIPIENT = UUID.randomUUID();

    @Test
    void linkingRegistersTheWearableDeviceLinkedEvent() {
        var device = new WearableDevice(new LinkWearableDeviceCommand(RECIPIENT, " GP-0001 ", "wristband"));

        assertThat(device.getDeviceType()).isEqualTo(DeviceType.WRISTBAND);
        assertThat(device.getSerialNumber().value()).isEqualTo("GP-0001");
        assertThat(device.getLinkedAt()).isNotNull();
        assertThat(device.domainEvents()).singleElement()
                .isInstanceOfSatisfying(WearableDeviceLinkedEvent.class, event -> {
                    assertThat(event.wearableDeviceId()).isEqualTo(device.getId());
                    assertThat(event.careRecipientProfileId()).isEqualTo(new CareRecipientProfileId(RECIPIENT));
                });
    }

    @Test
    void rejectsUnknownDeviceType() {
        assertThatThrownBy(() -> new WearableDevice(new LinkWearableDeviceCommand(RECIPIENT, "GP-0001", "PHONE")))
                .hasMessage("wearable-device.device-type.invalid");
    }

    @Test
    void onlyReportsForItsOwnCareRecipient() {
        var device = new WearableDevice(new LinkWearableDeviceCommand(RECIPIENT, "GP-0001", "SMARTWATCH"));

        assertThat(device.canReportFor(new CareRecipientProfileId(RECIPIENT))).isTrue();
        assertThat(device.canReportFor(new CareRecipientProfileId(UUID.randomUUID()))).isFalse();
    }
}
