package com.athena.cases.features.billing.enums.converter;

import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BillingHoldLevelCodeConverter implements AttributeConverter<BillingHoldLevelCode, String> {

    @Override
    public String convertToDatabaseColumn(BillingHoldLevelCode attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public BillingHoldLevelCode convertToEntityAttribute(String dbData) {
        return BillingHoldLevelCode.fromDbValue(dbData);
    }
}
