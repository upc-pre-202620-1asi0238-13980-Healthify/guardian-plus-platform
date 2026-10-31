package com.healthify.guardian.platform.subscriptions.infrastructure.scheduling;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class JdbcSubscriptionLifecycleCandidateFinderTest {

    @Test
    void shouldRejectNullEvaluationTime() {

        JdbcTemplate jdbcTemplate =
                mock(JdbcTemplate.class);

        JdbcSubscriptionLifecycleCandidateFinder finder =
                new JdbcSubscriptionLifecycleCandidateFinder(
                        jdbcTemplate
                );

        assertThrows(
                NullPointerException.class,
                () -> finder.findDueSubscriptionIds(null)
        );

        verifyNoInteractions(jdbcTemplate);
    }
}