package com.athena.cases.workflow;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.TeamRepository;
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

    public WorkflowServiceImpl(
            WorkflowRepository workflowRepository,
            CaseRepository caseRepository,
            TeamRepository teamRepository,
            CurrentUserService currentUserService) {
        this.workflowRepository = workflowRepository;
        this.caseRepository = caseRepository;
        this.teamRepository = teamRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public WorkflowRef start(StartWorkflowCommand command) {
        requireWfView();
        CaseEntity caseEntity = caseRepository.findById(command.caseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", String.valueOf(command.caseId())));
        TeamEntity team = teamRepository.findByCode(command.receiverTeamCode())
                .orElseThrow(() -> new ResourceNotFoundException("Team", command.receiverTeamCode()));

        Instant now = Instant.now();
        String actor = currentUserService.requirePrincipal().getUsername();
        WorkflowEntity entity = new WorkflowEntity();
        entity.setCaseId(caseEntity.getId());
        entity.setReceivingTeamId(team.getId());
        entity.setMessageKey("WF-" + caseEntity.getCaseNumber());
        entity.setMessageId("MSG-" + caseEntity.getId());
        entity.setMessageName(caseEntity.getSubject() + " Workflow");
        entity.setMessageObject(caseEntity.getCaseNumber());
        entity.setStatus(command.initialStatusCode() == null || command.initialStatusCode().isBlank()
                ? "Pending Assignment"
                : command.initialStatusCode());
        entity.setPriority(caseEntity.getPriority());
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

    private WorkflowQueueItem toQueueItem(WorkflowEntity w) {
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
                nullToEmpty(w.getActionLabel()),
                nullToEmpty(w.getLogs()));
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
        return new WorkflowRef(entity.getId(), entity.getCaseId(), teamCode, entity.getStatus());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
