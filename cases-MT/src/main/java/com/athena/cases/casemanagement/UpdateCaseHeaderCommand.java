package com.athena.cases.casemanagement;

/**
 * Command to update shared case header fields only (not feature extension columns).
 */
public record UpdateCaseHeaderCommand(
        String priority,
        String status,
        String assignedTo,
        Long parentCaseId,
        Long version
) {
}
