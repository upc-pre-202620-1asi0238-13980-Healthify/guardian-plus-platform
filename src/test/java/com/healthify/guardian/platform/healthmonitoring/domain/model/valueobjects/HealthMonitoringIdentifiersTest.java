package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HealthMonitoringIdentifiersTest {

    @Test
    void generatedIdsAreUnique() {
        assertThat(VitalSignId.generate()).isNotEqualTo(VitalSignId.generate());
        assertThat(VitalSignThresholdId.generate()).isNotEqualTo(VitalSignThresholdId.generate());
        assertThat(WearableDeviceId.generate()).isNotEqualTo(WearableDeviceId.generate());
        assertThat(VitalSignTypeId.generate()).isNotEqualTo(VitalSignTypeId.generate());
        assertThat(HealthReportId.generate()).isNotEqualTo(HealthReportId.generate());
    }

    @Test
    void idsWithSameUuidAreEqual() {
        UUID uuid = UUID.randomUUID();

        assertThat(new VitalSignId(uuid)).isEqualTo(new VitalSignId(uuid));
        assertThat(new CareRecipientProfileId(uuid)).isEqualTo(new CareRecipientProfileId(uuid));
    }

    @Test
    void idsRejectNull() {
        assertThatThrownBy(() -> new VitalSignId(null)).hasMessage("vital-sign.id.invalid");
        assertThatThrownBy(() -> new VitalSignThresholdId(null)).hasMessage("vital-sign-threshold.id.invalid");
        assertThatThrownBy(() -> new WearableDeviceId(null)).hasMessage("wearable-device.id.invalid");
        assertThatThrownBy(() -> new VitalSignTypeId(null)).hasMessage("vital-sign-type.id.invalid");
        assertThatThrownBy(() -> new HealthReportId(null)).hasMessage("health-report.id.invalid");
        assertThatThrownBy(() -> new CareRecipientProfileId(null)).hasMessage("care-recipient-profile.id.invalid");
        assertThatThrownBy(() -> new UserId(null)).hasMessage("user.id.invalid");
    }
}
