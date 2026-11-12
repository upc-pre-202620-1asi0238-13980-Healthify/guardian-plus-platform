package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.converters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * Converts {@link PersonUnderCareId} to/from a {@code UUID} column for JPA persistence.
 * Shared by every persistence entity in this bounded context that references a person under care.
 */
@Converter(autoApply = false)
public class PersonUnderCareIdPersistenceConverter implements AttributeConverter<PersonUnderCareId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(PersonUnderCareId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PersonUnderCareId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new PersonUnderCareId(dbData);
    }
}
