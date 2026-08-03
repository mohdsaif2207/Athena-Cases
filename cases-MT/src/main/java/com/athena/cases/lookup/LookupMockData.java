package com.athena.cases.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * In-memory mock lookup data until platform master-data APIs exist.
 */
public final class LookupMockData {

    public static final List<LookupItem> CLIENTS = List.of(
            item("CLIENT001", "ABC Bank"),
            item("CLIENT002", "XYZ Finance"),
            item("CLIENT003", "First National Credit Union"));

    public static final List<EventIdLookupItem> EVENT_IDS = List.of(
            new EventIdLookupItem("EVT1001", "Summer Campaign", "July"),
            new EventIdLookupItem("EVT1002", "Winter Campaign", "December"),
            new EventIdLookupItem("EVT1003", "Fall Acquisition", "October"));

    public static final List<LookupItem> SPOKEN_KEYS = List.of(
            item("SPK001", "English"),
            item("SPK002", "Spanish"),
            item("SPK003", "French"));

    // TEMPORARY MOCK DATA
    // Replace when the corresponding backend lookup API is implemented.
    public static final List<LookupItem> CAMPAIGNS = List.of(
            item("CMP001", "Q3 Retention"),
            item("CMP002", "New Member Drive"),
            item("CMP003", "Billing Hold Pilot"));

    // TEMPORARY MOCK DATA
    // Replace when the corresponding backend lookup API is implemented.
    // id is numeric so Billing Long productId / billingHoldByProductId validation can match.
    public static final List<LookupItem> PRODUCTS = List.of(
            new LookupItem("101", "PRD001", "Checking"),
            new LookupItem("102", "PRD002", "Savings"),
            new LookupItem("103", "PRD003", "Credit Card"),
            new LookupItem("104", "PCP001", "Northside Family PCP"),
            new LookupItem("105", "PCP002", "Riverside Primary Care"));

    // TEMPORARY MOCK DATA
    // Replace when the corresponding backend lookup API is implemented.
    // Keys match LookupMockData.CLIENTS id/code values.
    public static final Map<String, List<LookupItem>> SEGMENTS_BY_CLIENT = Map.of(
            "CLIENT001", List.of(
                    new LookupItem("201", "SEG201", "ABC Retail Segment"),
                    new LookupItem("202", "SEG202", "ABC Commercial Segment")),
            "CLIENT002", List.of(
                    new LookupItem("203", "SEG203", "XYZ Standard Segment"),
                    new LookupItem("204", "SEG204", "XYZ Premium Segment")),
            "CLIENT003", List.of(
                    new LookupItem("205", "SEG205", "FNCU Member Segment")));

    // TEMPORARY MOCK DATA — kept for reference / tests; findParentCases reads CaseRepository.
    // Replace when platform parent-case search API exists.
    public static final List<LookupItem> PARENT_CASES = List.of(
            new LookupItem("9001", "BIL9001", "BIL9001 — Sample Parent Billing Case"),
            new LookupItem("9002", "BIL9002", "BIL9002 — Sample Parent Research Case"),
            new LookupItem("9003", "DBM9003", "DBM9003 — Sample Parent DBM Case"));

    private LookupMockData() {
    }

    private static LookupItem item(String code, String label) {
        return new LookupItem(code, code, label);
    }

    /**
     * Segments for a client key (client id or code). Empty when unknown / blank.
     */
    public static List<LookupItem> segmentsForClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return List.of();
        }
        return SEGMENTS_BY_CLIENT.getOrDefault(clientId.trim(), List.of());
    }

    /** Flattened segment list (all clients) — used only for blank-query diagnostics / tests. */
    public static List<LookupItem> allSegments() {
        List<LookupItem> all = new ArrayList<>();
        for (List<LookupItem> rows : SEGMENTS_BY_CLIENT.values()) {
            all.addAll(rows);
        }
        return List.copyOf(all);
    }

    /**
     * Case-insensitive contains match on id, code, or label. Blank query returns the full list.
     */
    public static List<LookupItem> filterByQuery(List<LookupItem> source, String query) {
        if (query == null || query.isBlank()) {
            return source;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(item -> containsIgnoreCase(item.id(), q)
                        || containsIgnoreCase(item.code(), q)
                        || containsIgnoreCase(item.label(), q))
                .toList();
    }

    private static boolean containsIgnoreCase(String value, String queryLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(queryLower);
    }
}
