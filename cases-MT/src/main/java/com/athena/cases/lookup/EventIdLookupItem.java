package com.athena.cases.lookup;

/**
 * Lookup row for Event ID dropdowns (includes Mail Month for auto-populate).
 */
public record EventIdLookupItem(String code, String label, String mailMonth) {
}
