package com.athena.cases.workflow;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.TeamRepository;
import com.athena.cases.notification.NotificationEntity;
import com.athena.cases.notification.NotificationRepository;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    private final WorkflowRepository workflowRepository;
    private final CaseRepository caseRepository;
    private final TeamRepository teamRepository;
    private final CurrentUserService currentUserService;
    private final NotificationRepository notificationRepository;

    public WorkflowServiceImpl(
            WorkflowRepository workflowRepository,
            CaseRepository caseRepository,
            TeamRepository teamRepository,
            CurrentUserService currentUserService,
            NotificationRepository notificationRepository) {
        this.workflowRepository = workflowRepository;
        this.caseRepository = caseRepository;
        this.teamRepository = teamRepository;
        this.currentUserService = currentUserService;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public WorkflowRef start(StartWorkflowCommand command) {
        // Called as a side-effect of case create; list/get still require WF_VIEW + receiving team.
        currentUserService.requirePrincipal();
        TeamEntity team = teamRepository.findByCode(command.receiverTeamCode())
                .orElseThrow(() -> new ResourceNotFoundException("Team", command.receiverTeamCode()));

        // Platform cases table OR ExRT case_header (feature cases may not be in `cases`)
        CaseEntity caseEntity = caseRepository.findById(command.caseId()).orElse(null);
        String caseNumber = caseEntity != null ? caseEntity.getCaseNumber() : "CASE-" + command.caseId();
        String subject = caseEntity != null ? caseEntity.getSubject() : "ExRT Request";
        String priority = caseEntity != null ? caseEntity.getPriority() : "MEDIUM";

        Instant now = Instant.now();
        String actor = currentUserService.requirePrincipal().getUsername();
        WorkflowEntity entity = new WorkflowEntity();
        entity.setCaseId(command.caseId());
        entity.setReceivingTeamId(team.getId());
        entity.setMessageKey("WF-" + caseNumber);
        entity.setMessageId("MSG-" + command.caseId());
        entity.setMessageName(subject + " Workflow");
        entity.setMessageObject(caseNumber);
        entity.setStatus(command.initialStatusCode() == null || command.initialStatusCode().isBlank()
                ? "Pending Assignment"
                : command.initialStatusCode());
        entity.setPriority(priority);
        entity.setReceivedAt(now);
        entity.setActionLabel("Assign");
        entity.setOwnerName(team.getName());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        entity.setVersion(1);

        WorkflowEntity saved = workflowRepository.save(entity);
        log.info("workflow started - workflowId={} caseId={} team={}",
                saved.getId(), saved.getCaseId(), team.getCode());
        return toRef(saved, team.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowRef getByCaseId(Long caseId) {
        requireWfView();
        WorkflowEntity entity = workflowRepository.findFirstByCaseIdOrderByReceivedAtDesc(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow", "caseId=" + caseId));
        assertReceivingTeamAccess(entity.getReceivingTeamId());
        TeamEntity team = teamRepository.findById(entity.getReceivingTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", String.valueOf(entity.getReceivingTeamId())));
        return toRef(entity, team.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowQueueItem> listAuthorizedQueue() {
        requireWfView();
        UserPrincipal principal = currentUserService.requirePrincipal();
        List<String> receivingTeamCodes = principal.getReceivingTeamCodes();
        if (receivingTeamCodes.isEmpty()) {
            return List.of();
        }

        List<Long> teamIds = teamRepository.findByCodeIn(receivingTeamCodes).stream()
                .map(TeamEntity::getId)
                .toList();
        if (teamIds.isEmpty()) {
            return List.of();
        }

        return workflowRepository.findAuthorized(teamIds, false).stream()
                .map(this::toQueueItem)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowQueueItem updateQueueItem(Long workflowId, UpdateWorkflowCommand command) {
        requireWfView();
        WorkflowEntity entity = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow", String.valueOf(workflowId)));
        assertReceivingTeamAccess(entity.getReceivingTeamId());

        if (command.status() != null && !command.status().isBlank()) {
            entity.setStatus(command.status().trim());
        }
        if (command.decision() != null) {
            entity.setDecision(command.decision().trim());
        }
        if (command.priority() != null && !command.priority().isBlank()) {
            entity.setPriority(command.priority().trim());
        }
        if (command.owner() != null) {
            entity.setOwnerName(command.owner().trim());
        }
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(currentUserService.requirePrincipal().getUsername());

        WorkflowEntity saved = workflowRepository.save(entity);
        syncCaseFromWorkflow(saved);
        syncNotificationsFromWorkflow(saved);
        log.info("workflow queue updated - workflowId={} status={} user={}",
                saved.getId(), saved.getStatus(), entity.getUpdatedBy());
        return toQueueItem(saved);
    }

    private void syncCaseFromWorkflow(WorkflowEntity workflow) {
        caseRepository.findById(workflow.getCaseId()).ifPresent(caseEntity -> {
            if (workflow.getStatus() != null && !workflow.getStatus().isBlank()) {
                caseEntity.setCaseStatus(workflow.getStatus());
            }
            if (workflow.getPriority() != null && !workflow.getPriority().isBlank()) {
                caseEntity.setPriority(workflow.getPriority());
            }
            if (workflow.getOwnerName() != null && !workflow.getOwnerName().isBlank()) {
                caseEntity.setAssignedTo(workflow.getOwnerName());
            }
            caseEntity.setUpdatedAt(Instant.now());
            caseEntity.setUpdatedBy(currentUserService.requirePrincipal().getUsername());
            caseRepository.save(caseEntity);
        });
    }

    private void syncNotificationsFromWorkflow(WorkflowEntity workflow) {
        Instant now = Instant.now();
        String actor = currentUserService.requirePrincipal().getUsername();
        List<NotificationEntity> notifications =
                notificationRepository.findByCaseIdOrderByReceivedAtDesc(workflow.getCaseId());
        for (NotificationEntity n : notifications) {
            n.setMessage("Case queue update — workflow status: " + nullToEmpty(workflow.getStatus())
                    + ", priority: " + nullToEmpty(workflow.getPriority()));
            n.setUpdatedAt(now);
            n.setUpdatedBy(actor);
        }
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    private WorkflowQueueItem toQueueItem(WorkflowEntity w) {
        String teamCode = teamRepository.findById(w.getReceivingTeamId())
                .map(TeamEntity::getCode)
                .orElse("");
        return new WorkflowQueueItem(
                w.getId(),
                "WF-" + w.getId(),
                w.getCaseId(),
                w.getMessageKey(),
                w.getMessageId(),
                w.getMessageName(),
                nullToEmpty(w.getMessageObject()),
                w.getStatus(),
                nullToEmpty(w.getDecision()),
                nullToEmpty(w.getOwnerName()),
                w.getPriority(),
                w.getReceivedAt(),
                w.getUpdatedAt(),
                nullToEmpty(w.getActionLabel()),
                nullToEmpty(w.getLogs()),
                nullToEmpty(w.getCreatedBy()),
                teamCode);
    }

    private void assertReceivingTeamAccess(Long receivingTeamId) {
        TeamEntity team = teamRepository.findById(receivingTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", String.valueOf(receivingTeamId)));
        if (!currentUserService.requirePrincipal().getReceivingTeamCodes().contains(team.getCode())) {
            throw new ForbiddenException("Not authorized for receiving team " + team.getCode());
        }
    }

    private void requireWfView() {
        if (!currentUserService.hasPermission(PermissionCodes.WF_VIEW)) {
            throw new ForbiddenException("WF_VIEW required");
        }
    }

    private static WorkflowRef toRef(WorkflowEntity entity, String teamCode) {
        return new WorkflowRef(
                entity.getId(),
                entity.getCaseId(),
                entity.getMessageKey(),
                teamCode,
                entity.getStatus());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
