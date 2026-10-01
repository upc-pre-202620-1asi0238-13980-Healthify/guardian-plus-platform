package com.healthify.guardian.platform.emergencyalerting.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Infrastructure beans of the Emergency &amp; Alerting bounded context.
 */
@Configuration
public class EmergencyAlertingConfiguration {

    /**
     * Time source for every timing rule of this context (fall confirmation window, acknowledgement
     * timeout). Application services read the time from here instead of calling {@code Instant.now()},
     * so tests can pin or advance it.
     */
    @Bean
    public Clock emergencyAlertingClock() {
        return Clock.systemUTC();
    }
}
