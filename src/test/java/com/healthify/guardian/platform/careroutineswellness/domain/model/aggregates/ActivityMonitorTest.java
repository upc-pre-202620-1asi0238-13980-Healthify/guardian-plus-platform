package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ActivityResumedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MovementDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ProlongedInactivityDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.WalkDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivitySample;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.InactivityDetectionSettings;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityMonitorTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final SleepWindow NIGHT = new SleepWindow(LocalTime.of(22, 0), LocalTime.of(7, 0));
    // 15:00 and 23:00 in Lima
    private static final Instant AFTERNOON = Instant.parse("2026-10-08T20:00:00Z");
    private static final Instant NIGHT_TIME = Instant.parse("2026-10-09T04:00:00Z");

    private ActivityMonitor monitor;

    @BeforeEach
    void createMonitor() {
        monitor = new ActivityMonitor(new PersonUnderCareId(UUID.randomUUID()));
    }

    private void still(Instant at, int inactiveMinutes) {
        monitor.recordSample(new ActivitySample(at, 0, BigDecimal.valueOf(inactiveMinutes)), NIGHT, LIMA);
    }

    private void moving(Instant at, int steps) {
        monitor.recordSample(new ActivitySample(at, steps, BigDecimal.ZERO), NIGHT, LIMA);
    }

    @Test
    void reachingTheThresholdDuringTheDayRaisesProlongedInactivityOnce() {
        still(AFTERNOON, 55);
        assertThat(monitor.domainEvents()).isEmpty();

        still(AFTERNOON.plusSeconds(10), 60);
        still(AFTERNOON.plusSeconds(20), 65);

        assertThat(monitor.getStatus()).isEqualTo(ActivityStatus.INACTIVITY_DETECTED);
        assertThat(monitor.domainEvents()).singleElement().isInstanceOf(ProlongedInactivityDetectedEvent.class);
    }

    @Test
    void doesNotWatchInactivityDuringTheSleepWindow() {
        still(NIGHT_TIME, 120);

        assertThat(monitor.getStatus()).isEqualTo(ActivityStatus.NORMAL);
        assertThat(monitor.domainEvents()).isEmpty();
    }

    @Test
    void honoursTheConfiguredThresholdAndSwitch() {
        monitor.configureDetection(new InactivityDetectionSettings(true, 90));
        still(AFTERNOON, 60);
        assertThat(monitor.isInactive()).isFalse();

        monitor.configureDetection(new InactivityDetectionSettings(false, 90));
        still(AFTERNOON.plusSeconds(10), 120);
        assertThat(monitor.isInactive()).isFalse();
    }

    @Test
    void rejectsAThresholdOutOfRange() {
        assertThatThrownBy(() -> new InactivityDetectionSettings(true, 10))
                .hasMessage("activity-monitor.threshold-minutes.invalid");
    }

    @Test
    void movingAfterALongPauseResetsTheCounterWithoutAlarm() {
        still(AFTERNOON, 70);
        monitor.clearDomainEvents();

        moving(AFTERNOON.plusSeconds(10), 30);

        assertThat(monitor.getStatus()).isEqualTo(ActivityStatus.NORMAL);
        assertThat(monitor.getInactiveMinutes()).isEqualByComparingTo("0");
        assertThat(monitor.getLastMovementAt()).isEqualTo(AFTERNOON.plusSeconds(10));
        assertThat(monitor.domainEvents()).hasSize(2)
                .hasAtLeastOneElementOfType(MovementDetectedEvent.class)
                .hasAtLeastOneElementOfType(ActivityResumedEvent.class);
    }

    @Test
    void aShortPauseIsNotLogged() {
        still(AFTERNOON, 5);
        moving(AFTERNOON.plusSeconds(10), 30);

        assertThat(monitor.domainEvents()).isEmpty();
    }

    @Test
    void aLongBurstOfMovementIsLoggedAsAWalkWhenItEnds() {
        moving(AFTERNOON, 80);
        moving(AFTERNOON.plusSeconds(300), 90);
        still(AFTERNOON.plusSeconds(720), 5);

        assertThat(monitor.domainEvents()).singleElement()
                .isInstanceOfSatisfying(WalkDetectedEvent.class, walk -> {
                    assertThat(walk.steps()).isEqualTo(170);
                    assertThat(walk.startedAt()).isEqualTo(AFTERNOON);
                    assertThat(walk.endedAt()).isEqualTo(AFTERNOON.plusSeconds(720));
                });
    }

    @Test
    void ignoresSamplesOlderThanTheLastOne() {
        still(AFTERNOON, 30);
        still(AFTERNOON.minusSeconds(60), 90);

        assertThat(monitor.getInactiveMinutes()).isEqualByComparingTo("30");
        assertThat(monitor.isInactive()).isFalse();
    }
}
