package com.athena.cases.workflow;

import java.time.Instant;

/**
 * Workflow Queue grid row.
 */
public record WorkflowQueueItem(
        Long id,
        String workflowId,
        Long caseId,
        String messageKey,
        String messageId,
        String messageName,
        String messageObject,
        String status,
        String decision,
        String owner,
        String priority,
        Instant receivedAt,
        Instant updatedAt,
        String action,
        String logs,
        String createdBy,
        String receivingTeamCode
) {
}
