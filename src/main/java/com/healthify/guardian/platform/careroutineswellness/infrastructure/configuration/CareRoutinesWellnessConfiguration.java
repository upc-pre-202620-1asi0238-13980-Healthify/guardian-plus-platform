package com.healthify.guardian.platform.careroutineswellness.infrastructure.configuration;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReissueTolerance;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RestockThreshold;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Externalizes the platform-wide configuration values this bounded context needs, so that
 * none of them are hardcoded in domain or application code. Each value is exposed as the domain
 * value object that carries its meaning.
 *
 * <p>{@link SleepWindow} is exposed as a single platform-wide default because the domain model
 * documented for this context does not (yet) include a per-person-under-care preferences
 * aggregate; once one exists (most likely in the {@code Profile} bounded context), this bean
 * should be replaced by a lookup keyed on {@code PersonUnderCareId}.</p>
 */
@Configuration
public class CareRoutinesWellnessConfiguration {

    @Bean
    public ZoneId careRoutinesWellnessZoneId(
            @Value("${care-routines-wellness.zone-id:America/Lima}") String zoneId) {
        return ZoneId.of(zoneId);
    }

    @Bean
    public SleepWindow careRoutinesWellnessDefaultSleepWindow(
            @Value("${care-routines-wellness.sleep-window.start:22:00}") String start,
            @Value("${care-routines-wellness.sleep-window.end:07:00}") String end) {
        return new SleepWindow(LocalTime.parse(start), LocalTime.parse(end));
    }

    @Bean
    public RestockThreshold careRoutinesWellnessRestockThreshold(
            @Value("${care-routines-wellness.medication-stock.restock-threshold-days:3}") String days) {
        return new RestockThreshold(new BigDecimal(days));
    }

    @Bean
    public ReissueTolerance careRoutinesWellnessReissueTolerance(
            @Value("${care-routines-wellness.reminder.reissue-tolerance-minutes:10}") long minutes) {
        return new ReissueTolerance(Duration.ofMinutes(minutes));
    }
}
