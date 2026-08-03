package com.athena.cases.workflow;

import java.util.List;

/**
 * Port for workflow instance start and query.
 * Real engine adapter is Lead-owned; features call this port only.
 */
public interface WorkflowService {

    WorkflowRef start(StartWorkflowCommand command);

    WorkflowRef getByCaseId(Long caseId);

    List<WorkflowQueueItem> listAuthorizedQueue();
}
