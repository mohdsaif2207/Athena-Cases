package com.athena.cases.features.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Billing Extract Type — LLD §20.3.
 */
public enum BillingExtractType {

    BILLING("Billing"),
    PRE_NOTE("Pre Note"),
    REBILL("Rebill");

    private final String dbValue;

    BillingExtractType(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static BillingExtractType fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        for (BillingExtractType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown BillingExtractType: " + dbValue);
    }
}
