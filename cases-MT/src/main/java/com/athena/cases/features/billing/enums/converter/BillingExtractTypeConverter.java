package com.athena.cases.features.billing.enums.converter;

import com.athena.cases.features.billing.enums.BillingExtractType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BillingExtractTypeConverter implements AttributeConverter<BillingExtractType, String> {

    @Override
    public String convertToDatabaseColumn(BillingExtractType attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public BillingExtractType convertToEntityAttribute(String dbData) {
        return BillingExtractType.fromDbValue(dbData);
    }
}
