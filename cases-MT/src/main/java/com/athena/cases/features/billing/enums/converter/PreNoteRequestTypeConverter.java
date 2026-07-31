package com.athena.cases.features.billing.enums.converter;

import com.athena.cases.features.billing.enums.PreNoteRequestType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PreNoteRequestTypeConverter implements AttributeConverter<PreNoteRequestType, String> {

    @Override
    public String convertToDatabaseColumn(PreNoteRequestType attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public PreNoteRequestType convertToEntityAttribute(String dbData) {
        return PreNoteRequestType.fromDbValue(dbData);
    }
}
