package com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

/**
 * Base JPA persistence entity for all persistence entities that require auditing.
 *
 * <p>Provides {@code id}, {@code createdAt}, and {@code updatedAt} fields.
 * This class intentionally lives in the infrastructure layer to keep JPA and
 * Spring Data auditing concerns out of the domain model.</p>
 *
 * <p>All bounded-context JPA persistence entities should extend this class
 * instead of placing {@code @Id} and auditing fields directly.</p>
 *
 * <p>The identity is an application-assigned {@link UUID} rather than a database-generated
 * sequence: every aggregate root generates its own business identifier (e.g. {@code ReminderId})
 * at construction time, so the very same value is reused as the technical primary key here.
 * The id is therefore never null, which is exactly what lets Spring Data always route
 * {@code save()} through {@code EntityManager.merge()} instead of {@code persist()} — merge()
 * reconciles a detached copy with whatever instance is already managed in the current
 * persistence context (relevant with {@code open-in-view=true}, where a find-then-save within
 * the same request shares one session), whereas persist() would reject it outright with
 * "a different object with the same identifier is already associated with this session".
 * Auditing still fires correctly either way: {@code @CreatedDate}/{@code @LastModifiedDate}
 * are driven by Hibernate's insert/update lifecycle events, not by how {@code save()} was invoked.</p>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractPersistenceEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

    /**
     * Sets the id. Used by assemblers when reconstructing a persistence entity
     * from an existing domain object that already carries an identity.
     *
     * @param id the persistence identity to assign
     */
    public void setId(UUID id) {
        this.id = id;
    }
}
