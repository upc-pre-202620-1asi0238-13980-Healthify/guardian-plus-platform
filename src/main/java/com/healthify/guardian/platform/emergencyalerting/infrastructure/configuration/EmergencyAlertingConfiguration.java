package com.healthify.guardian.platform.emergencyalerting.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;

/**
 * Infrastructure beans of the Emergency &amp; Alerting bounded context.
 */
@Configuration
@EnableAsync
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

    /**
     * Dedicated executor for sending notifications, so a slow provider never blocks the thread that
     * dispatched the alert nor competes with other asynchronous work of the platform.
     */
    @Bean
    public ThreadPoolTaskExecutor emergencyAlertingNotificationExecutor(
            @Value("${emergency-alerting.notifications.executor.pool-size:4}") int poolSize) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("ea-notify-");
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }
}
