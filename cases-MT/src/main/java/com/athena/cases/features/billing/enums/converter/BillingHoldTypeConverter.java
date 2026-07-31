package com.athena.cases.features.billing.enums.converter;

import com.athena.cases.features.billing.enums.BillingHoldType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BillingHoldTypeConverter implements AttributeConverter<BillingHoldType, String> {

    @Override
    public String convertToDatabaseColumn(BillingHoldType attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public BillingHoldType convertToEntityAttribute(String dbData) {
        return BillingHoldType.fromDbValue(dbData);
    }
}
