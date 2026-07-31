package com.athena.cases.casemanagement;

/**
 * Immutable reference returned by {@link CaseManagementService}.
 *
 * @param caseId     internal surrogate key ({@code cases.id})
 * @param caseNumber display Case ID shown to users
 * @param version    optimistic lock token
 */
public record CaseRef(Long caseId, String caseNumber, Long version) {
}
