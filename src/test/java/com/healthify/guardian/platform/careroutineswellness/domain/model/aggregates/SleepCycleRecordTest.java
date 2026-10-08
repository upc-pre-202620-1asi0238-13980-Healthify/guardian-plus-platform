package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordSleepCycleCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepClassification;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SleepCycleRecordTest {

    private static final UUID PERSON = UUID.randomUUID();
    private static final Instant BEDTIME = Instant.parse("2026-10-08T03:48:00Z");
    private static final Instant WAKE_UP = Instant.parse("2026-10-08T11:36:00Z");

    private static SleepCycleRecord night(int interruptions) {
        return new SleepCycleRecord(new RecordSleepCycleCommand(PERSON, BEDTIME, WAKE_UP, interruptions));
    }

    @Test
    void moreThanFourInterruptionsIsAFragmentedNight() {
        assertThat(night(4).getClassification()).isEqualTo(SleepClassification.REGULAR);
        assertThat(night(5).getClassification()).isEqualTo(SleepClassification.FRAGMENTED);
    }

    @Test
    void measuresDurationAndContinuity() {
        var record = night(0);

        assertThat(record.durationMinutes()).isEqualTo(468);
        assertThat(record.continuityScore()).isEqualTo(98);
        assertThat(night(4).continuityScore()).isEqualTo(49);
        assertThat(night(8).continuityScore()).isZero();
    }

    @Test
    void rejectsANightThatEndsBeforeItStarts() {
        assertThatThrownBy(() -> new SleepCycleRecord(new RecordSleepCycleCommand(PERSON, WAKE_UP, BEDTIME, 0)))
                .hasMessage("sleep-cycle-record.times.invalid");
    }
}
