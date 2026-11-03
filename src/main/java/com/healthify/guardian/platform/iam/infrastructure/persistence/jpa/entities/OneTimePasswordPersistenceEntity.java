package com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for one-time codes. Holds the hash of the code only.
 */
@Entity
@Table(name = "otp_codes", indexes = @Index(name = "ix_otp_codes_user_account_purpose",
        columnList = "user_account_id, purpose"))
@Getter
@Setter
@NoArgsConstructor
public class OneTimePasswordPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_account_id", nullable = false)
    private UUID userAccountId;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;
}
