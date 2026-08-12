package com.athena.cases.features.billing.lookup;

import com.athena.cases.lookup.LookupItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Billing-owned temporary lookup datasets.
 *
 * <p>Isolated from shared {@code com.athena.cases.lookup.LookupMockData} so Billing
 * can evolve without merge conflicts against shared / DBM / ExRT lookup work.
 */
public final class BillingLookupMockData {

    // TEMP: Replace with shared lookup API when available.
    // Flat list kept for unfiltered callers (clientId == null).
    public static final List<LookupItem> CAMPAIGNS = List.of(
            item("CMP001", "Q3 Retention"),
            item("CMP002", "New Member Drive"),
            item("CMP003", "Billing Hold Pilot"));

    // TEMP: Replace with shared lookup API when available.
    // Keys match shared client codes (CLIENT001…) — Campaign ID depends on Client Name.
    public static final Map<String, List<LookupItem>> CAMPAIGNS_BY_CLIENT = Map.of(
            "CLIENT001", List.of(
                    item("CMP001", "Q3 Retention"),
                    item("CMP002", "New Member Drive")),
            "CLIENT002", List.of(
                    item("CMP002", "New Member Drive"),
                    item("CMP003", "Billing Hold Pilot")),
            "CLIENT003", List.of(
                    item("CMP001", "Q3 Retention"),
                    item("CMP003", "Billing Hold Pilot")));

    // TEMP: Replace with shared lookup API when available.
    // id is numeric so Billing Long productId / billingHoldByProductId validation can match.
    public static final List<LookupItem> PRODUCTS = List.of(
            new LookupItem("101", "PRD001", "Checking"),
            new LookupItem("102", "PRD002", "Savings"),
            new LookupItem("103", "PRD003", "Credit Card"),
            new LookupItem("104", "PCP001", "Northside Family PCP"),
            new LookupItem("105", "PCP002", "Riverside Primary Care"));

    // TEMP: Replace with shared lookup API when available.
    // Keys match shared client codes (CLIENT001…) until Billing has its own client source.
    public static final Map<String, List<LookupItem>> SEGMENTS_BY_CLIENT = Map.of(
            "CLIENT001", List.of(
                    new LookupItem("201", "SEG201", "ABC Retail Segment"),
                    new LookupItem("202", "SEG202", "ABC Commercial Segment")),
            "CLIENT002", List.of(
                    new LookupItem("203", "SEG203", "XYZ Standard Segment"),
                    new LookupItem("204", "SEG204", "XYZ Premium Segment")),
            "CLIENT003", List.of(
                    new LookupItem("205", "SEG205", "FNCU Member Segment")));

    private BillingLookupMockData() {
    }

    private static LookupItem item(String code, String label) {
        return new LookupItem(code, code, label);
    }

    /** Segments for a client key (client id or code). Empty when unknown / blank. */
    public static List<LookupItem> segmentsForClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return List.of();
        }
        return SEGMENTS_BY_CLIENT.getOrDefault(clientId.trim(), List.of());
    }

    /**
     * Campaigns for a client key. Empty when blank/unknown.
     * {@code null} clientId returns the full campaign list (unfiltered / backward compatible).
     */
    public static List<LookupItem> campaignsForClient(String clientId) {
        if (clientId == null) {
            return CAMPAIGNS;
        }
        if (clientId.isBlank()) {
            return List.of();
        }
        return CAMPAIGNS_BY_CLIENT.getOrDefault(clientId.trim(), List.of());
    }

    /** Case-insensitive contains match on id, code, or label. Blank query returns the full list. */
    public static List<LookupItem> filterByQuery(List<LookupItem> source, String query) {
        if (query == null || query.isBlank()) {
            return source;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<LookupItem> matched = new ArrayList<>();
        for (LookupItem item : source) {
            if (containsIgnoreCase(item.id(), q)
                    || containsIgnoreCase(item.code(), q)
                    || containsIgnoreCase(item.label(), q)) {
                matched.add(item);
            }
        }
        return List.copyOf(matched);
    }

    private static boolean containsIgnoreCase(String value, String queryLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(queryLower);
    }
}
