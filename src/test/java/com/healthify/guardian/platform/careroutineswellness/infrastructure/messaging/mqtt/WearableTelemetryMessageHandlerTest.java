package com.healthify.guardian.platform.careroutineswellness.infrastructure.messaging.mqtt;

import com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices.ActivityMonitorCommandServiceImpl;
import com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices.SleepCycleRecordCommandServiceImpl;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepClassification;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityMonitorRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.SleepCycleRecordRepository;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.ActivityTelemetryConsumer;
import com.healthify.guardian.platform.careroutineswellness.interfaces.messaging.SleepTelemetryConsumer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of {@link WearableTelemetryMessageHandler} with the payloads published by the IoT simulator.
 */
class WearableTelemetryMessageHandlerTest {

    private static final String ACTIVITY_TOPIC = "guardian/activity/GP-WB-001";
    private static final String SLEEP_TOPIC = "guardian/sleep/GP-WB-001";
    private static final UUID PERSON = UUID.randomUUID();

    private final InMemoryActivityMonitorRepository monitors = new InMemoryActivityMonitorRepository();
    private final InMemorySleepCycleRecordRepository nights = new InMemorySleepCycleRecordRepository();
    private final WearableTelemetryMessageHandler handler = new WearableTelemetryMessageHandler(
            new ActivityTelemetryConsumer(new ActivityMonitorCommandServiceImpl(
                    monitors, new SleepWindow(LocalTime.of(22, 0), LocalTime.of(7, 0)), ZoneId.of("America/Lima"))),
            new SleepTelemetryConsumer(new SleepCycleRecordCommandServiceImpl(nights)));

    // same shape as the simulator's Signal.to_dict(), including the fields this context ignores
    private static byte[] activity(String signalType, String measuredAt, double inactiveMinutes, int steps) {
        return """
                {"deviceId":"GP-WB-001","careRecipientProfileId":"%s","signalType":"%s","severity":"INFO",
                 "measuredAt":"%s","inactiveMinutes":%s,"thresholdMinutes":60,"steps":%d,
                 "stepsAccumulated":1520,"movementIntensity":0.0}
                """.formatted(PERSON, signalType, measuredAt, inactiveMinutes, steps).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] sleep(String recipient) {
        return """
                {"deviceId":"GP-WB-001","careRecipientProfileId":"%s","signalType":"SLEEP_CYCLE_RECORDED",
                 "severity":"WARNING","measuredAt":"2026-10-08T11:36:00.123456+00:00","sleepHours":7.4,
                 "interruptions":5,"continuityIndex":0.35,"classification":"FRAGMENTED","maxInterruptions":4}
                """.formatted(recipient).getBytes(StandardCharsets.UTF_8);
    }

    private ActivityMonitor monitor() {
        return monitors.findByPersonUnderCareId(new PersonUnderCareId(PERSON)).orElseThrow();
    }

    @Test
    void activitySamplesFeedTheMonitorWhichAppliesTheThreshold() {
        // 15:00 in Lima: inside the watch hours
        assertThat(handler.handleActivity(ACTIVITY_TOPIC,
                activity("ACTIVITY_SAMPLE", "2026-10-08T20:00:00.000001+00:00", 55.0, 0))).isTrue();
        assertThat(monitor().getStatus()).isEqualTo(ActivityStatus.NORMAL);

        handler.handleActivity(ACTIVITY_TOPIC, activity("ACTIVITY_SAMPLE", "2026-10-08T20:00:10+00:00", 65.0, 0));

        assertThat(monitor().getStatus()).isEqualTo(ActivityStatus.INACTIVITY_DETECTED);
        assertThat(monitor().getInactiveMinutes()).isEqualByComparingTo("65");
    }

    @Test
    void ignoresTheWearableVerdictsBecauseTheSampleOfTheSameCycleCarriesThem() {
        assertThat(handler.handleActivity(ACTIVITY_TOPIC,
                activity("PROLONGED_INACTIVITY", "2026-10-08T20:00:00+00:00", 65.0, 0))).isFalse();
        assertThat(handler.handleActivity(ACTIVITY_TOPIC,
                activity("ACTIVITY_RESUMED", "2026-10-08T20:00:00+00:00", 65.0, 40))).isFalse();

        assertThat(monitors.findByPersonUnderCareId(new PersonUnderCareId(PERSON))).isEmpty();
    }

    @Test
    void sleepReportBecomesAClosedNightEndingAtMeasuredAt() {
        assertThat(handler.handleSleep(SLEEP_TOPIC, sleep(PERSON.toString()))).isTrue();

        assertThat(nights.saved).singleElement().satisfies(night -> {
            assertThat(night.getEndTime()).isEqualTo(Instant.parse("2026-10-08T11:36:00.123456Z"));
            assertThat(night.durationMinutes()).isEqualTo(444);
            assertThat(night.getInterruptionCount()).isEqualTo(5);
            assertThat(night.getClassification()).isEqualTo(SleepClassification.FRAGMENTED);
        });
    }

    @Test
    void neverThrowsOnMalformedMessages() {
        assertThat(handler.handleSleep(SLEEP_TOPIC, sleep("not-a-uuid"))).isFalse();
        assertThat(handler.handleActivity(ACTIVITY_TOPIC, "{not json".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(nights.saved).isEmpty();
    }

    private static final class InMemoryActivityMonitorRepository implements ActivityMonitorRepository {

        private final Map<PersonUnderCareId, ActivityMonitor> monitors = new HashMap<>();

        @Override
        public Optional<ActivityMonitor> findByPersonUnderCareId(PersonUnderCareId personUnderCareId) {
            return Optional.ofNullable(monitors.get(personUnderCareId));
        }

        @Override
        public ActivityMonitor save(ActivityMonitor monitor) {
            monitor.clearDomainEvents();
            monitors.put(monitor.getPersonUnderCareId(), monitor);
            return monitor;
        }
    }

    private static final class InMemorySleepCycleRecordRepository implements SleepCycleRecordRepository {

        private final List<SleepCycleRecord> saved = new ArrayList<>();

        @Override
        public List<SleepCycleRecord> findByPersonUnderCareId(PersonUnderCareId personUnderCareId, Instant from, Instant to) {
            return List.copyOf(saved);
        }

        @Override
        public SleepCycleRecord save(SleepCycleRecord record) {
            record.clearDomainEvents();
            saved.add(record);
            return record;
        }
    }
}
