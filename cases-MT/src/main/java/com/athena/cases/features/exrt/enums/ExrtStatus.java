package com.athena.cases.features.exrt.enums;

public enum ExrtStatus {
    REQUESTED_EXRT("Requested - ExRT"),
    IN_REVIEW("In Review"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String label;

    ExrtStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
