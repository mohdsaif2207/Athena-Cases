package com.athena.cases.workflow;

public record StartWorkflowCommand(
        Long caseId,
        String receiverTeamCode,
        String initialStatusCode
) {
}
