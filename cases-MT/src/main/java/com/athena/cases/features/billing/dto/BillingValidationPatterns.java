package com.athena.cases.features.billing.dto;

/**
 * Bean Validation patterns for case-header String fields (no shared Priority/Status enums yet).
 * Billing-typed fields use Billing enums instead of these patterns.
 */
public final class BillingValidationPatterns {

    public static final String PRIORITY = "High|Medium|Low";

    public static final String STATUS =
            "Requested|In Progress|Killed|On Hold|Incomplete|Completed";

    private BillingValidationPatterns() {
    }
}
