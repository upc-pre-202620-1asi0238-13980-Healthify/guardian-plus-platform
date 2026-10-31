package com.healthify.guardian.platform.profile.infrastructure.configuration;

import com.healthify.guardian.platform.profile.domain.repositories.CareRelationshipRepository;
import com.healthify.guardian.platform.profile.domain.services.CareRelationshipPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ProfileConfiguration {

    @Bean
    public Clock profileClock() {
        return Clock.systemUTC();
    }

    @Bean
    public CareRelationshipPolicy careRelationshipPolicy(
            CareRelationshipRepository careRelationshipRepository) {

        return new CareRelationshipPolicy(
                careRelationshipRepository);
    }
}