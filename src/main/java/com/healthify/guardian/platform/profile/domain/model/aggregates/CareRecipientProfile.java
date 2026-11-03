package com.healthify.guardian.platform.profile.domain.model.aggregates;

import com.healthify.guardian.platform.profile.domain.model.commands.CreateCareRecipientProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.events.CareRecipientProfileCreatedEvent;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Aggregate root representing a person under care in Guardian+.
 */
@Getter
public class CareRecipientProfile extends AbstractDomainAggregateRoot<CareRecipientProfile> {

    private static final String INVALID_PERSONAL_INFORMATION_MESSAGE_KEY =
            "care-recipient-profile.personal-information.invalid";
    private static final String INVALID_BIRTH_DATE_MESSAGE_KEY =
            "care-recipient-profile.birth-date.invalid";
    private static final String INVALID_TIMESTAMP_MESSAGE_KEY =
            "care-recipient-profile.timestamp.invalid";

    private CareRecipientProfileId id;
    private UserId createdByUserId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String profileImageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public CareRecipientProfile() {
    }

    /**
     * Creates a new profile for a person under care.
     */
    public CareRecipientProfile(CreateCareRecipientProfileCommand command, Instant createdAt) {
        if (command == null || command.createdByUserId() == null) {
            throw new IllegalArgumentException("user.id.invalid");
        }

        validatePersonalInformation(command.firstName(), command.lastName());
        validateBirthDate(command.birthDate());
        validateTimestamp(createdAt);

        this.id = CareRecipientProfileId.generate();
        this.createdByUserId = new UserId(command.createdByUserId());
        this.firstName = command.firstName();
        this.lastName = command.lastName();
        this.birthDate = command.birthDate();
        this.profileImageUrl = command.profileImageUrl();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;

        registerDomainEvent(CareRecipientProfileCreatedEvent.from(this));
    }

    /**
     * Updates the personal information of the person under care.
     */
    public void updatePersonalInformation(
            String firstName,
            String lastName,
            LocalDate birthDate,
            Instant updatedAt) {

        validatePersonalInformation(firstName, lastName);
        validateBirthDate(birthDate);
        validateTimestamp(updatedAt);

        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.updatedAt = updatedAt;
    }

    /**
     * Updates the profile image.
     */
    public void updateProfileImage(String profileImageUrl, Instant updatedAt) {
        validateTimestamp(updatedAt);

        this.profileImageUrl = profileImageUrl;
        this.updatedAt = updatedAt;
    }

    private static void validatePersonalInformation(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(INVALID_PERSONAL_INFORMATION_MESSAGE_KEY);
        }
    }

    private static void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException(INVALID_BIRTH_DATE_MESSAGE_KEY);
        }
    }

    private static void validateTimestamp(Instant timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException(INVALID_TIMESTAMP_MESSAGE_KEY);
        }
    }

    /** Restores the aggregate state from persistence. Used by the persistence assembler. */
    public void setId(CareRecipientProfileId id) {
        this.id = id;
    }

    public void setCreatedByUserId(UserId createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}