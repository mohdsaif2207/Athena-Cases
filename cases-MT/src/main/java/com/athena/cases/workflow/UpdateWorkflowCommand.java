package com.athena.cases.workflow;

/**
 * Partial update for a Workflow Queue row (receiving-team editors).
 */
public record UpdateWorkflowCommand(
        String status,
        String decision,
        String priority,
        String owner
) {
}
