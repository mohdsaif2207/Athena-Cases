package com.athena.cases.features.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Billing Request Type — LLD §20.3 / user story.
 * Persisted value matches PostgreSQL CHECK (title case / display labels).
 */
public enum BillingRequestType {

    COMPLIANCE_LEGAL("Compliance/Legal"),
    EXTRACT("Extract"),
    OPERATIONS_ALERT("Operations Alert"),
    REJECT_REVIEW("Reject Review"),
    RESEARCH("Research"),
    SCHEDULE("Schedule");

    private final String dbValue;

    BillingRequestType(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static BillingRequestType fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        for (BillingRequestType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown BillingRequestType: " + dbValue);
    }
}
