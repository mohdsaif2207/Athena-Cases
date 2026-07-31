package com.athena.cases.workflow;

public record WorkflowRef(
        Long workflowInstanceId,
        Long caseId,
        String receiverTeamCode,
        String statusCode
) {
}
