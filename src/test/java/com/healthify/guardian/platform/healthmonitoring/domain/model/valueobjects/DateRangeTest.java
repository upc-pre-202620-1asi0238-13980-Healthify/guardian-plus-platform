package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    private static final LocalDate START = LocalDate.of(2026, 9, 28);
    private static final LocalDate END = LocalDate.of(2026, 10, 4);

    @Test
    void containsBothBoundsAndDaysInBetween() {
        DateRange week = new DateRange(START, END);

        assertThat(week.contains(START)).isTrue();
        assertThat(week.contains(END)).isTrue();
        assertThat(week.contains(LocalDate.of(2026, 10, 1))).isTrue();
    }

    @Test
    void doesNotContainDaysOutsideOrNull() {
        DateRange week = new DateRange(START, END);

        assertThat(week.contains(START.minusDays(1))).isFalse();
        assertThat(week.contains(END.plusDays(1))).isFalse();
        assertThat(week.contains(null)).isFalse();
    }

    @Test
    void allowsSingleDayRange() {
        assertThat(new DateRange(START, START).contains(START)).isTrue();
    }

    @Test
    void rejectsStartAfterEnd() {
        assertThatThrownBy(() -> new DateRange(END, START)).hasMessage("date-range.invalid");
    }

    @Test
    void rejectsNullBounds() {
        assertThatThrownBy(() -> new DateRange(null, END)).hasMessage("date-range.invalid");
        assertThatThrownBy(() -> new DateRange(START, null)).hasMessage("date-range.invalid");
    }
}
