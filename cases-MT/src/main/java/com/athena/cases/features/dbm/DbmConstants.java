package com.athena.cases.features.dbm;

/**
 * DBM Work Order Request constants — teams, deep links, transfer-type rules.
 */
public final class DbmConstants {

    public static final String CASE_TYPE_CODE = "DBM_WORK_ORDER_REQUEST";

    /** Seeded receiving team code ({@code teams.code}); display name is "DBM". */
    public static final String RECEIVER_TEAM_DBM = "DBM_TEAM";

    public static final String WORKFLOW_STATUS_PENDING_ASSIGNMENT = "Pending Assignment";

    public static final String TRANSFER_TYPE_OTHER = "Other (Requires Description)";

    public static final String TRANSFER_TYPE_CUSTOM = "Custom";

    public static final String TRANSFER_TYPE_CUSTOM_REQUIRES_APPROVAL = "Custom (Requires Approval)";

    /**
     * After create / notification View — Cases grid (shared Case Details route does not exist).
     */
    public static final String CASE_DEEP_LINK = "/cases";

    public static String caseDeepLink(Long caseId) {
        // caseId reserved for future /cases/:id; grid is the current destination.
        return CASE_DEEP_LINK;
    }

    public static String newCaseNotificationMessage(String caseNumber) {
        return "A new DBM Work Order Request (Case #" + caseNumber
                + ") has been assigned to your team.";
    }

    public static String updatedCaseNotificationMessage(String caseNumber) {
        return "DBM Work Order Request (Case #" + caseNumber
                + ") has been updated.";
    }

    private DbmConstants() {
    }
}
