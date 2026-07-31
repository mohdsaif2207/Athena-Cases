package com.athena.cases.workflow.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athena.cases.security.CurrentUserService;
import com.athena.cases.workflow.StartWorkflowCommand;
import com.athena.cases.workflow.WorkflowRef;
import com.athena.cases.workflow.WorkflowService;
import com.athena.cases.workflow.entity.Workflow;
import com.athena.cases.workflow.repository.WorkflowRepository;

/**
 * Shared workflow persistence adapter. Features call {@link WorkflowService} only.
 */
@Service
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final CurrentUserService currentUserService;

    public WorkflowServiceImpl(
            WorkflowRepository workflowRepository,
            CurrentUserService currentUserService) {
        this.workflowRepository = workflowRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public WorkflowRef start(StartWorkflowCommand command) {
        if (command == null || command.caseId() == null) {
            throw new IllegalStateException("caseId is required to start a workflow");
        }
        if (isBlank(command.workflowType())) {
            throw new IllegalStateException("workflowType is required to start a workflow");
        }
        if (isBlank(command.receiverTeamCode())) {
            throw new IllegalStateException("receiverTeamCode is required to start a workflow");
        }
        if (isBlank(command.initialStatusCode())) {
            throw new IllegalStateException("initialStatusCode is required to start a workflow");
        }
        if (workflowRepository.existsByCaseId(command.caseId())) {
            throw new IllegalStateException(
                    "Workflow already exists for caseId: " + command.caseId());
        }

        Instant now = Instant.now();
        String actor = currentUserService.requireUserId();

        Workflow workflow = new Workflow();
        workflow.setCaseId(command.caseId());
        workflow.setWorkflowType(command.workflowType().trim());
        workflow.setStatus(command.initialStatusCode().trim());
        workflow.setAssignedTeam(command.receiverTeamCode().trim());
        workflow.setCreatedAt(now);
        workflow.setUpdatedAt(now);
        workflow.setCreatedBy(actor);
        workflow.setUpdatedBy(actor);

        Workflow saved = workflowRepository.save(workflow);
        return toRef(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowRef getByCaseId(Long caseId) {
        Workflow workflow = workflowRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalStateException(
                        "Workflow not found for caseId: " + caseId));
        return toRef(workflow);
    }

    private static WorkflowRef toRef(Workflow workflow) {
        return new WorkflowRef(
                workflow.getId(),
                workflow.getCaseId(),
                workflow.getWorkflowType(),
                workflow.getAssignedTeam(),
                workflow.getStatus());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
