package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.converters;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * Converts {@link CareRecipientProfileId} to/from a {@code UUID} column. Shared by every persistence
 * entity of this bounded context that references a care recipient.
 */
@Converter(autoApply = false)
public class CareRecipientProfileIdPersistenceConverter implements AttributeConverter<CareRecipientProfileId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(CareRecipientProfileId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public CareRecipientProfileId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new CareRecipientProfileId(dbData);
    }
}
