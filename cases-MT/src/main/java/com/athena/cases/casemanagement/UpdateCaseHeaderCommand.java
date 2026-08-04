package com.athena.cases.casemanagement;

import java.time.LocalDate;

/**
 * Command to update shared case header fields only (not feature extension columns).
 */
public record UpdateCaseHeaderCommand(
        String priority,
        String status,
        String assignedTo,
        Long parentCaseId,
        Long version,
        String subject,
        String description,
        String clientId,
        LocalDate requestedDueDate
) {
    /** Backward-compatible ctor used by Billing feature create/update. */
    public UpdateCaseHeaderCommand(
            String priority,
            String status,
            String assignedTo,
            Long parentCaseId,
            Long version) {
        this(priority, status, assignedTo, parentCaseId, version, null, null, null, null);
    }
}
