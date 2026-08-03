package com.athena.cases.features.exrt.constant;

public final class ExrtConstants {

    public static final String API_BASE = "/api/v1/cases/exrt-requests";
    public static final String LOOKUP_BASE = "/api/v1/lookups/exrt";
    public static final String DEFAULT_STATUS = "Requested - ExRT";
    public static final String DEFAULT_PRIORITY = "MEDIUM";
    /** Seeded case-type / workflow message key for ExRT. */
    public static final String WORKFLOW_TYPE_EXRT = "EXRT_REQUEST";
    /** Seeded team code in {@code teams.code} (display name is "DBM"). */
    public static final String RECEIVER_TEAM_DBM = "DBM_TEAM";
    public static final String WORKFLOW_STATUS_PENDING_ASSIGNMENT = "PENDING_ASSIGNMENT";
    public static final String CASE_DETAILS_DEEP_LINK = "/cases/%s";

    private ExrtConstants() {
    }
}
