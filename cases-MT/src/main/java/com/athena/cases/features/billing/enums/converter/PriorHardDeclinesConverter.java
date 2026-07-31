package com.athena.cases.features.billing.enums.converter;

import com.athena.cases.features.billing.enums.PriorHardDeclines;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PriorHardDeclinesConverter implements AttributeConverter<PriorHardDeclines, String> {

    @Override
    public String convertToDatabaseColumn(PriorHardDeclines attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public PriorHardDeclines convertToEntityAttribute(String dbData) {
        return PriorHardDeclines.fromDbValue(dbData);
    }
}
