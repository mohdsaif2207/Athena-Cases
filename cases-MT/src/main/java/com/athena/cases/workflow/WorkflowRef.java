package com.athena.cases.workflow;

/**
 * Reference to a persisted workflow instance.
 */
public record WorkflowRef(
        Long workflowInstanceId,
        Long caseId,
        String workflowType,
        String receiverTeamCode,
        String statusCode
) {
}
