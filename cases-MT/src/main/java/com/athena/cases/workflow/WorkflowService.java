package com.athena.cases.workflow;

/**
 * Port for workflow instance start and query.
 * Real engine adapter is Lead-owned; features call this port only.
 */
public interface WorkflowService {

    WorkflowRef start(StartWorkflowCommand command);

    WorkflowRef getByCaseId(Long caseId);

    /** Queue rows visible to the caller's receiving teams (requires WF_VIEW). */
    java.util.List<WorkflowQueueItem> listAuthorizedQueue();
}
