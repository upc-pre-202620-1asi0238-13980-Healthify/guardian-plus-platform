package com.healthify.guardian.platform.healthmonitoring.testsupport;

import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Minimal in-memory store shared by the fake repositories. Like the JPA adapters, saving publishes
 * the aggregate's registered domain events; they are snapshotted and cleared first, so handlers that
 * save the same aggregate again (Detect -> Emit -> Evaluate) do not interfere.
 */
class InMemoryAggregateStore<K, A extends AbstractDomainAggregateRoot<A>> {

    private final Map<K, A> items = new LinkedHashMap<>();
    private final Function<A, K> idOf;
    private final ApplicationEventPublisher eventPublisher;

    InMemoryAggregateStore(Function<A, K> idOf, ApplicationEventPublisher eventPublisher) {
        this.idOf = idOf;
        this.eventPublisher = eventPublisher;
    }

    A save(A aggregate) {
        items.put(idOf.apply(aggregate), aggregate);
        var events = List.copyOf(aggregate.domainEvents());
        aggregate.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        return aggregate;
    }

    List<A> saveAll(List<A> aggregates) {
        aggregates.forEach(aggregate -> items.put(idOf.apply(aggregate), aggregate));
        aggregates.forEach(aggregate -> {
            var events = List.copyOf(aggregate.domainEvents());
            aggregate.clearDomainEvents();
            events.forEach(eventPublisher::publishEvent);
        });
        return aggregates;
    }

    Optional<A> findById(K id) {
        return Optional.ofNullable(items.get(id));
    }

    List<A> findAll(Predicate<A> filter) {
        return new ArrayList<>(items.values().stream().filter(filter).toList());
    }
}
