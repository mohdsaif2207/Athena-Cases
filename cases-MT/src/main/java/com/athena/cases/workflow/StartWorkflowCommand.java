package com.athena.cases.workflow;

/**
 * Command to start a workflow instance for a case.
 *
 * @param caseId            internal {@code cases.id}
 * @param workflowType      persisted as {@code workflows.workflow_type} (e.g. DBM_WORK_ORDER)
 * @param receiverTeamCode  persisted as {@code workflows.assigned_team} (e.g. DBM)
 * @param initialStatusCode persisted as {@code workflows.status} (e.g. PENDING_ASSIGNMENT)
 */
public record StartWorkflowCommand(
        Long caseId,
        String workflowType,
        String receiverTeamCode,
        String initialStatusCode
) {
}
