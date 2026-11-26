package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PaymentOperationPersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PaymentOperationPersistenceRepository
        extends JpaRepository<PaymentOperationPersistenceEntity, String> {

    Optional<PaymentOperationPersistenceEntity>
    findByExternalReference(
            String externalReference
    );

    @Transactional(propagation = Propagation.MANDATORY)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT operation
            FROM PaymentOperationPersistenceEntity operation
            WHERE operation.idempotencyKey = :idempotencyKey
            """)
    Optional<PaymentOperationPersistenceEntity>
    findByIdForUpdate(
            @Param("idempotencyKey") String idempotencyKey
    );

    @Transactional(propagation = Propagation.MANDATORY)
    @Modifying
    @Query(
            value = """
                    INSERT INTO subscription_payment_operations (
                        idempotency_key,
                        subscription_id,
                        subscriber_id,
                        plan_id,
                        provider,
                        purpose,
                        amount,
                        currency,
                        current_period_end,
                        external_reference,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        :idempotencyKey,
                        :subscriptionId,
                        :subscriberId,
                        :planId,
                        :provider,
                        :purpose,
                        :amount,
                        :currency,
                        :currentPeriodEnd,
                        NULL,
                        :createdAt,
                        :createdAt
                    )
                    ON CONFLICT (idempotency_key)
                    DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("idempotencyKey") String idempotencyKey,
            @Param("subscriptionId") UUID subscriptionId,
            @Param("subscriberId") UUID subscriberId,
            @Param("planId") UUID planId,
            @Param("provider") String provider,
            @Param("purpose") String purpose,
            @Param("amount") BigDecimal amount,
            @Param("currency") String currency,
            @Param("currentPeriodEnd") Instant currentPeriodEnd,
            @Param("createdAt") Instant createdAt
    );
}