package com.athena.cases.features.billing;

/**
 * Billing-owned workflow / notification constants — LLD §24 / §25.
 */
public final class BillingConstants {

    public static final String RECEIVER_TEAM_BILLING_OPS = "BILLING_OPS";

    public static final String WORKFLOW_STATUS_PENDING_ASSIGNMENT = "PENDING_ASSIGNMENT";

    /** Deep-link pattern — approved Phase-1 decision #11. */
    public static final String CASE_DEEP_LINK_PATTERN = "/cases/%d";

    /**
     * TODO Replace with actual implementation after module integration —
     * If Cases Search routing changes, update this pattern in one place.
     */
    public static String caseDeepLink(Long caseId) {
        return CASE_DEEP_LINK_PATTERN.formatted(caseId);
    }

    public static String newCaseNotificationMessage(String caseNumber) {
        return "A new Billing Department Request (Case #" + caseNumber
                + ") has been assigned to your team.";
    }

    public static final String DEFAULT_PRIORITY = "Medium";

    public static final String DEFAULT_STATUS = "Requested";

    private BillingConstants() {
    }
}
