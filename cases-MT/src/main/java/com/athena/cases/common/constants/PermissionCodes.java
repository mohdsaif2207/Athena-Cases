package com.athena.cases.common.constants;

/**
 * Central permission codes. Exact product list may expand — keep additions Lead-owned.
 * Codes must match rows seeded in Flyway IAM migrations.
 */
public final class PermissionCodes {

    public static final String ADMIN_ACCESS = "ADMIN_ACCESS";
    public static final String CASES_ACCESS = "CASES_ACCESS";
    public static final String CASES_CREATE = "CASES_CREATE";
    public static final String CASES_EDIT = "CASES_EDIT";
    public static final String CASES_VIEW = "CASES_VIEW";
    public static final String WF_VIEW = "WF_VIEW";
    public static final String NOTIF_VIEW = "NOTIF_VIEW";

    private PermissionCodes() {
    }
}
