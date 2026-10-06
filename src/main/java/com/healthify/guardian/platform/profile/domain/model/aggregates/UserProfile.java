package com.healthify.guardian.platform.profile.domain.model.aggregates;

import com.healthify.guardian.platform.profile.domain.model.commands.CreateUserProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.events.ContactInformationUpdatedEvent;
import com.healthify.guardian.platform.profile.domain.model.events.ProfileCreatedEvent;
import com.healthify.guardian.platform.profile.domain.model.events.ProfileUpdatedEvent;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root representing the descriptive profile associated with an IAM user.
 *
 * <p>Authentication credentials remain owned by the IAM bounded context. This aggregate
 * only keeps the referenced user identifier together with descriptive and contact data.</p>
 */
@Getter
public class UserProfile extends AbstractDomainAggregateRoot<UserProfile> {

    private static final String INVALID_PERSONAL_INFORMATION_MESSAGE_KEY =
            "user-profile.personal-information.invalid";
    private static final String INVALID_CONTACT_INFORMATION_MESSAGE_KEY =
            "user-profile.contact-information.invalid";
    private static final String INVALID_TIMESTAMP_MESSAGE_KEY =
            "user-profile.timestamp.invalid";

    private UserProfileId id;
    private UserId userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public UserProfile() {
    }

    /**
     * Creates the descriptive profile of an IAM user.
     */
    public UserProfile(CreateUserProfileCommand command, Instant createdAt) {
        if (command == null || command.userId() == null) {
            throw new IllegalArgumentException("user.id.invalid");
        }
        validatePersonalInformation(command.firstName(), command.lastName());
        validateTimestamp(createdAt);

        this.id = UserProfileId.generate();
        this.userId = new UserId(command.userId());
        this.firstName = command.firstName();
        this.lastName = command.lastName();
        this.phoneNumber = command.phoneNumber();
        this.profileImageUrl = command.profileImageUrl();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;

        registerDomainEvent(ProfileCreatedEvent.from(this));
    }

    /**
     * Updates the user's descriptive personal information.
     */
    public void updatePersonalInformation(String firstName, String lastName, Instant updatedAt) {
        validatePersonalInformation(firstName, lastName);
        validateTimestamp(updatedAt);

        this.firstName = firstName;
        this.lastName = lastName;
        this.updatedAt = updatedAt;

        registerDomainEvent(ProfileUpdatedEvent.from(this));
    }

    /**
     * Updates the user's contact phone number.
     */
    public void updateContactInformation(String phoneNumber, Instant updatedAt) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(INVALID_CONTACT_INFORMATION_MESSAGE_KEY);
        }
        validateTimestamp(updatedAt);

        this.phoneNumber = phoneNumber;
        this.updatedAt = updatedAt;

        registerDomainEvent(ContactInformationUpdatedEvent.from(this));
    }

    /**
     * Updates the profile image.
     */
    public void updateProfileImage(String profileImageUrl, Instant updatedAt) {
        validateTimestamp(updatedAt);

        this.profileImageUrl = profileImageUrl;
        this.updatedAt = updatedAt;

        registerDomainEvent(ProfileUpdatedEvent.from(this));
    }

    private static void validatePersonalInformation(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(INVALID_PERSONAL_INFORMATION_MESSAGE_KEY);
        }
    }

    private static void validateTimestamp(Instant timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException(INVALID_TIMESTAMP_MESSAGE_KEY);
        }
    }

    /** Restores the aggregate state from persistence. Used by the persistence assembler. */
    public void setId(UserProfileId id) {
        this.id = id;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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