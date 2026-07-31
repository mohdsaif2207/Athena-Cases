package com.athena.cases.features.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Billing Hold Level dual-listbox codes — LLD §13.3 / §20.3.
 *
 * <p>TODO Replace after BA finalizes Hold Type mapping —
 * Interim: flat Available set for every {@link BillingHoldType}.
 */
public enum BillingHoldLevelCode {

    ALL("All"),
    AUTO_CANCEL("Auto Cancel"),
    BILLING("Billing"),
    PRE_NOTE("Pre Note"),
    REBILL("Rebill"),
    REFUND("Refund");

    private final String dbValue;

    BillingHoldLevelCode(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static BillingHoldLevelCode fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        for (BillingHoldLevelCode code : values()) {
            if (code.dbValue.equals(dbValue)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown BillingHoldLevelCode: " + dbValue);
    }
}
