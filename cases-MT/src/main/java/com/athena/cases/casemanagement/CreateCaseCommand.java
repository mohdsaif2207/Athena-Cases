package com.athena.cases.casemanagement;

/**
 * Command to create a shared case header.
 * Field set is intentionally minimal until foundation Phase 5 finalizes the spine schema.
 */
public record CreateCaseCommand(
        String caseTypeCode,
        String caseOwner,
        String priority,
        String status,
        Long parentCaseId
) {
}
