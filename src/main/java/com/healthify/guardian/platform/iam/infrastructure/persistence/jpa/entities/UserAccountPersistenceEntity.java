package com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountStatus;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA persistence entity for user accounts. Holds hashes only: never a password or a reset token in plain text.
 */
@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor
public class UserAccountPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserAccountStatus status;

    @Column(name = "password_reset_token_hash", unique = true, length = 64)
    private String passwordResetTokenHash;

    @Column(name = "password_reset_token_expires_at")
    private Instant passwordResetTokenExpiresAt;
}
