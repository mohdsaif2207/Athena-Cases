package com.athena.cases.casemanagement;

/**
 * Generates durable business case numbers from PostgreSQL sequences.
 * Reusable by ExRT, Billing, DBM, and other case-type modules.
 */
public interface CaseNumberGenerator {

    /**
     * Advances {@code sequenceName} by one and formats {@code prefix + zero-padded value}.
     *
     * @param sequenceName PostgreSQL sequence (e.g. {@code exrt_case_number_seq})
     * @param prefix       business prefix (e.g. {@code ExR})
     * @param padWidth     digit width (e.g. {@code 6} → {@code ExR000001})
     */
    String next(String sequenceName, String prefix, int padWidth);
}
