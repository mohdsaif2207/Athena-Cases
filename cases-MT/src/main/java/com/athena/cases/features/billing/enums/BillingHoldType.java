package com.athena.cases.features.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Billing Hold Type — LLD §20.3.
 */
public enum BillingHoldType {

    CLIENT_LEVEL("Client Level"),
    COVERAGE_LEVEL("Coverage Level"),
    PRODUCT_LEVEL("Product Level"),
    SEGMENT_LEVEL("Segment Level");

    private final String dbValue;

    BillingHoldType(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static BillingHoldType fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        for (BillingHoldType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown BillingHoldType: " + dbValue);
    }
}
