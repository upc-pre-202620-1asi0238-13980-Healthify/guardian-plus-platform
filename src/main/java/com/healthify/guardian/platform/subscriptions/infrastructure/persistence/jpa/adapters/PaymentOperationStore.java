package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PaymentOperationPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.PaymentOperationPersistenceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
public class PaymentOperationStore {

    private final PaymentOperationPersistenceRepository repository;

    public PaymentOperationStore(
            PaymentOperationPersistenceRepository repository
    ) {
        this.repository =
                Objects.requireNonNull(repository);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentOperationSnapshot reserve(
            PaymentRequest request,
            String provider
    ) {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );

        Objects.requireNonNull(
                provider,
                "provider must not be null"
        );

        if (provider.isBlank()) {

            throw new IllegalArgumentException(
                    "provider must not be blank"
            );
        }

        Instant now =
                Instant.now()
                        .truncatedTo(ChronoUnit.MICROS);

        repository.insertIfAbsent(
                request.idempotencyKey(),
                request.subscriptionId(),
                request.subscriberId(),
                request.planId(),
                provider,
                request.purpose().name(),
                request.amount(),
                request.currency().getCurrencyCode(),
                request.currentPeriodEnd(),
                now
        );

        PaymentOperationPersistenceEntity operation =
                findOperationForUpdate(
                        request.idempotencyKey()
                );

        validateOperation(
                operation,
                request,
                provider
        );

        return toSnapshot(operation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentOperationSnapshot registerExternalReference(
            PaymentRequest request,
            String provider,
            String externalReference
    ) {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );

        PaymentOperationPersistenceEntity operation =
                findOperationForUpdate(
                        request.idempotencyKey()
                );

        validateOperation(
                operation,
                request,
                provider
        );

        operation.registerExternalReference(
                externalReference,
                Instant.now()
        );

        repository.saveAndFlush(operation);

        return toSnapshot(operation);
    }

    private PaymentOperationPersistenceEntity findOperationForUpdate(
            String idempotencyKey
    ) {

        return repository
                .findByIdForUpdate(idempotencyKey)
                .orElseThrow(
                        () -> new SubscriptionPaymentException(
                                "Payment operation not found"
                        )
                );
    }

    private void validateOperation(
            PaymentOperationPersistenceEntity operation,
            PaymentRequest request,
            String provider
    ) {

        boolean matches =
                operation.getSubscriptionId()
                        .equals(request.subscriptionId())

                && operation.getSubscriberId()
                        .equals(request.subscriberId())

                && operation.getPlanId()
                        .equals(request.planId())

                && operation.getProvider()
                        .equals(provider)

                && operation.getPurpose()
                        == request.purpose()

                && operation.getAmount()
                        .compareTo(request.amount()) == 0

                && operation.getCurrency()
                        .equals(request.currency().getCurrencyCode())

                && Objects.equals(
                        operation.getCurrentPeriodEnd(),
                        request.currentPeriodEnd()
                );

        if (!matches) {

            throw new SubscriptionPaymentException(
                    "Payment operation conflicts with the original request"
            );
        }
    }

    private PaymentOperationSnapshot toSnapshot(
            PaymentOperationPersistenceEntity operation
    ) {

        return new PaymentOperationSnapshot(
                operation.getIdempotencyKey(),
                operation.getExternalReference(),
                operation.getCreatedAt()
        );
    }

    public record PaymentOperationSnapshot(
            String idempotencyKey,
            String externalReference,
            Instant createdAt
    ) {

        public boolean hasExternalReference() {

            return externalReference != null;
        }
    }
}