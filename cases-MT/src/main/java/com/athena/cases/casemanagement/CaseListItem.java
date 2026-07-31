package com.athena.cases.casemanagement;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Case row for the Cases Search grid.
 */
public record CaseListItem(
        Long id,
        String caseId,
        String caseType,
        String caseTypeCode,
        String clientId,
        String subject,
        String description,
        String caseOwner,
        String caseStatus,
        Instant createdAt,
        Instant updatedAt,
        LocalDate requestedDueDate,
        String carrier,
        String priority,
        String assignedTo,
        String segmentId,
        String frequency,
        String spokenKey,
        String eventId,
        String mailMonth
) {
}
