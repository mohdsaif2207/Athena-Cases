package com.athena.cases.features.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Do Coverages Have Prior Hard Declines? — LLD §20.3 (Yes / No).
 */
public enum PriorHardDeclines {

    YES("Yes"),
    NO("No");

    private final String dbValue;

    PriorHardDeclines(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static PriorHardDeclines fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        for (PriorHardDeclines value : values()) {
            if (value.dbValue.equals(dbValue)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown PriorHardDeclines: " + dbValue);
    }
}
