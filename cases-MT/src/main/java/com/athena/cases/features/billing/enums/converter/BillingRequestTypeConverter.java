package com.athena.cases.features.billing.enums.converter;

import com.athena.cases.features.billing.enums.BillingRequestType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BillingRequestTypeConverter implements AttributeConverter<BillingRequestType, String> {

    @Override
    public String convertToDatabaseColumn(BillingRequestType attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public BillingRequestType convertToEntityAttribute(String dbData) {
        return BillingRequestType.fromDbValue(dbData);
    }
}
