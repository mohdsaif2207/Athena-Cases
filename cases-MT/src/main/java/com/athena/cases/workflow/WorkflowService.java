package com.athena.cases.workflow;

/**
 * Port for workflow instance start and query.
 * Real engine adapter is Lead-owned; features call this port only.
 */
public interface WorkflowService {

    WorkflowRef start(StartWorkflowCommand command);

    WorkflowRef getByCaseId(Long caseId);
}
