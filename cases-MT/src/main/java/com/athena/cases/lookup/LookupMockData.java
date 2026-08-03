package com.athena.cases.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory mock lookup data until platform master-data APIs exist.
 * Shared by Billing, DBM, and ExRT form dropdowns where applicable.
 */
public final class LookupMockData {

    public static final List<LookupItem> CLIENTS = List.of(
            item("CLIENT001", "ABC Bank"),
            item("CLIENT002", "XYZ Finance"),
            item("CLIENT003", "First National Credit Union"));

    /** Mail month uses yyyy-MM (develop) so DBM auto-populate stays compatible. */
    public static final List<EventIdLookupItem> EVENT_IDS = List.of(
            new EventIdLookupItem("EVT1001", "Summer Campaign", "2025-07"),
            new EventIdLookupItem("EVT1002", "Winter Campaign", "2025-12"),
            new EventIdLookupItem("EVT1003", "Fall Acquisition", "2025-10"));

    public static final List<LookupItem> SPOKEN_KEYS = List.of(
            item("SPK001", "English"),
            item("SPK002", "Spanish"),
            item("SPK003", "French"));

    // TEMPORARY MOCK DATA — Billing campaigns (stable CMP codes).
    public static final List<LookupItem> CAMPAIGNS = List.of(
            item("CMP001", "Q3 Retention"),
            item("CMP002", "New Member Drive"),
            item("CMP003", "Billing Hold Pilot"));

    // TEMPORARY MOCK DATA — numeric id so Billing Long productId validation can match.
    public static final List<LookupItem> PRODUCTS = List.of(
            new LookupItem("101", "PRD001", "Checking"),
            new LookupItem("102", "PRD002", "Savings"),
            new LookupItem("103", "PRD003", "Credit Card"),
            new LookupItem("104", "PCP001", "Northside Family PCP"),
            new LookupItem("105", "PCP002", "Riverside Primary Care"));

    // TEMPORARY MOCK DATA — keys match CLIENTS id/code values.
    public static final Map<String, List<LookupItem>> SEGMENTS_BY_CLIENT = Map.of(
            "CLIENT001", List.of(
                    new LookupItem("201", "SEG201", "ABC Retail Segment"),
                    new LookupItem("202", "SEG202", "ABC Commercial Segment")),
            "CLIENT002", List.of(
                    new LookupItem("203", "SEG203", "XYZ Standard Segment"),
                    new LookupItem("204", "SEG204", "XYZ Premium Segment")),
            "CLIENT003", List.of(
                    new LookupItem("205", "SEG205", "FNCU Member Segment")));

    // TEMPORARY MOCK DATA — findParentCases prefers CaseRepository; kept for reference/tests.
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

    public static Optional<LookupItem> findClient(String id) {
        return findById(CLIENTS, id);
    }

    public static Optional<LookupItem> findCampaign(String id) {
        return findById(CAMPAIGNS, id);
    }

    public static Optional<LookupItem> findProduct(String id) {
        return findById(PRODUCTS, id);
    }

    public static Optional<LookupItem> findSegment(String id) {
        return findById(allSegments(), id);
    }

    public static Optional<LookupItem> findParentCase(String id) {
        return findById(PARENT_CASES, id);
    }

    private static Optional<LookupItem> findById(List<LookupItem> items, String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return items.stream()
                .filter(i -> id.equals(i.id()) || id.equals(i.code()))
                .findFirst();
    }

    private static boolean containsIgnoreCase(String value, String queryLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(queryLower);
    }
}
