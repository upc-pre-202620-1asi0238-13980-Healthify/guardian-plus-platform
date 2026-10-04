package com.healthify.guardian.platform.profile.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Infrastructure beans of the Profile bounded context.
 */
@Configuration
public class ProfileConfiguration {

    /**
     * Time source for profile creation and updates.
     * Application services read the time from here instead of calling Instant.now(),
     * so tests can use a deterministic clock.
     */
    @Bean
    public Clock profileClock() {
        return Clock.systemUTC();
    }
}