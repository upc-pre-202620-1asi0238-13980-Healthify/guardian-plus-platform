package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderIssuedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.interfaces.events.AppointmentReminderIssuedIntegrationEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of {@link AppointmentReminderIssuedEventHandler}.
 */
class AppointmentReminderIssuedEventHandlerTest {

    private final List<Object> published = new ArrayList<>();
    private final AppointmentReminderIssuedEventHandler handler = new AppointmentReminderIssuedEventHandler(published::add);

    private final UUID reminder = UUID.randomUUID();
    private final UUID person = UUID.randomUUID();
    private final Instant appointmentTime = Instant.parse("2026-11-13T15:30:00Z");
    private final Instant issuedAt = Instant.parse("2026-11-13T14:30:00Z");

    @Test
    void anIssuedAppointmentReminderIsRepublishedForEmergencyAndAlerting() {
        handler.on(issued(ReminderType.APPOINTMENT, "Control de cardiología", "Clínica San Pablo"));

        assertThat(published).containsExactly(new AppointmentReminderIssuedIntegrationEvent(
                reminder, person, "Control de cardiología", "Clínica San Pablo", appointmentTime, issuedAt));
    }

    @Test
    void otherIssuedRemindersAreNotRepublished() {
        handler.on(issued(ReminderType.MEDICATION, "Losartán", null));
        handler.on(issued(ReminderType.HYDRATION, "Agua", null));

        assertThat(published).isEmpty();
    }

    private ReminderIssuedEvent issued(ReminderType type, String title, String location) {
        return new ReminderIssuedEvent(
                new ReminderId(reminder), new PersonUnderCareId(person), type, title, location, appointmentTime, issuedAt);
    }
}
