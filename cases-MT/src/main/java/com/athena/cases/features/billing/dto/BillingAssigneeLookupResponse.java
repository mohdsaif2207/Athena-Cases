package com.athena.cases.features.billing.dto;

import com.athena.cases.lookup.LookupItem;

/**
 * Billing assignee option — LLD §16.5.
 * Mapped from shared {@link LookupItem} ({@code code} → username, {@code label} → displayName).
 */
public record BillingAssigneeLookupResponse(String username, String displayName) {

    public static BillingAssigneeLookupResponse from(LookupItem item) {
        return new BillingAssigneeLookupResponse(item.code(), item.label());
    }
}
