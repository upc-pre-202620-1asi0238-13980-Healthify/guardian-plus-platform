package com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications;

import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.ReminderNotification;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of the appointment reminder texts of {@link PushNotificationTexts}.
 */
class PushNotificationTextsTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Test
    void anAppointmentReminderSaysWhatWhenAndWhereInTheLocalTime() {
        var content = PushNotificationTexts.of(
                reminder("Ana Pérez", "Control de cardiología", "Clínica San Pablo"), LIMA);

        assertThat(content.title()).isEqualTo("Cita próxima: Control de cardiología");
        // 15:30 UTC is 10:30 in Lima
        assertThat(content.body()).isEqualTo("Ana Pérez tiene su cita el 13/11 a las 10:30 en Clínica San Pablo.");
    }

    @Test
    void missingNameAndLocationFallBackToNeutralTexts() {
        var content = PushNotificationTexts.of(reminder(null, "Control de cardiología", null), LIMA);

        assertThat(content.body()).isEqualTo("La persona bajo cuidado tiene su cita el 13/11 a las 10:30.");
    }

    private static ReminderNotification reminder(String name, String title, String location) {
        return new ReminderNotification(
                UUID.randomUUID(),
                new CareRecipientProfileId(UUID.randomUUID()),
                name,
                title,
                location,
                Instant.parse("2026-11-13T15:30:00Z"),
                new UserId(UUID.randomUUID()),
                "device-token");
    }
}
