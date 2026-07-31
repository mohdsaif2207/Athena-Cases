package com.athena.cases.features.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Pre Note Request Type — LLD §20.3.
 */
public enum PreNoteRequestType {

    ALL("All"),
    CHANGES_ONLY("Changes Only");

    private final String dbValue;

    PreNoteRequestType(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static PreNoteRequestType fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        for (PreNoteRequestType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PreNoteRequestType: " + dbValue);
    }
}
