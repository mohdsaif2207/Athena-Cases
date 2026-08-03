package com.athena.cases.lookup;

import java.util.List;

/**
 * In-memory mock lookup data until platform master-data APIs exist.
 */
public final class LookupMockData {

    public static final List<LookupItem> CLIENTS = List.of(
            item("CLIENT001", "ABC Bank"),
            item("CLIENT002", "XYZ Finance"),
            item("CLIENT003", "First National Credit Union"));

    public static final List<EventIdLookupItem> EVENT_IDS = List.of(
            new EventIdLookupItem("EVT1001", "Summer Campaign", "2025-07"),
            new EventIdLookupItem("EVT1002", "Winter Campaign", "2025-12"),
            new EventIdLookupItem("EVT1003", "Fall Acquisition", "2025-10"));

    public static final List<LookupItem> SPOKEN_KEYS = List.of(
            item("SPK001", "English"),
            item("SPK002", "Spanish"),
            item("SPK003", "French"));

    public static final List<LookupItem> CAMPAIGNS = List.of(
            item("CMP001", "Q3 Retention"),
            item("CMP002", "New Member Drive"));

    public static final List<LookupItem> PRODUCTS = List.of(
            item("PRD001", "Checking"),
            item("PRD002", "Savings"),
            item("PRD003", "Credit Card"));

    private LookupMockData() {
    }

    private static LookupItem item(String code, String label) {
        return new LookupItem(code, code, label);
    }
}
