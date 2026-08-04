package com.athena.cases.features.billing;

/**
 * Billing-owned workflow / notification constants — LLD §24 / §25.
 */
public final class BillingConstants {

    public static final String CASE_TYPE_CODE = "BILLING_DEPARTMENT_REQUEST";

    public static final String RECEIVER_TEAM_BILLING_OPS = "BILLING_OPS_TEAM";

    public static final String WORKFLOW_STATUS_PENDING_ASSIGNMENT = "PENDING_ASSIGNMENT";

    /**
     * Opens Billing view/edit page (query-param details route until /cases/:id exists).
     */
    public static final String CASE_DEEP_LINK_PATTERN = "/cases/new/billing?caseId=%d&mode=view";

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
