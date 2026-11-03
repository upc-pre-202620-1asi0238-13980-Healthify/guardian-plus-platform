package com.healthify.guardian.platform.subscriptions.infrastructure.scheduling;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.SubscriptionLifecycleCandidateFinder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class JdbcSubscriptionLifecycleCandidateFinder
        implements SubscriptionLifecycleCandidateFinder {

    private static final String FIND_DUE_SUBSCRIPTIONS_SQL = """
            SELECT id
            FROM subscriptions
            WHERE status IN ('ACTIVE', 'CANCELLATION_SCHEDULED')
              AND current_period_end IS NOT NULL
              AND current_period_end <= ?
            ORDER BY current_period_end
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcSubscriptionLifecycleCandidateFinder(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate =
                Objects.requireNonNull(jdbcTemplate);
    }

    @Override
    public List<UUID> findDueSubscriptionIds(
            Instant evaluatedAt
    ) {

        Objects.requireNonNull(
                evaluatedAt,
                "evaluatedAt must not be null"
        );

        return jdbcTemplate.query(
                FIND_DUE_SUBSCRIPTIONS_SQL,
                (resultSet, rowNumber) ->
                        UUID.fromString(
                                resultSet.getString("id")
                        ),
                Timestamp.from(evaluatedAt)
        );
    }
}