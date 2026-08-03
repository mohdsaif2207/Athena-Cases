package com.athena.cases.casemanagement;

import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import com.athena.cases.workflow.StartWorkflowCommand;
import com.athena.cases.workflow.WorkflowService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaseManagementServiceImpl implements CaseManagementService {

    private static final Logger log = LoggerFactory.getLogger(CaseManagementServiceImpl.class);

    private final CaseRepository caseRepository;
    private final CaseTypeRepository caseTypeRepository;
    private final CurrentUserService currentUserService;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;

    public CaseManagementServiceImpl(
            CaseRepository caseRepository,
            CaseTypeRepository caseTypeRepository,
            CurrentUserService currentUserService,
            WorkflowService workflowService,
            NotificationService notificationService) {
        this.caseRepository = caseRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.currentUserService = currentUserService;
        this.workflowService = workflowService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public CaseRef createCase(CreateCaseCommand command) {
        requireCasesCreate();
        CaseTypeEntity caseType = caseTypeRepository.findByCode(command.caseTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("CaseType", command.caseTypeCode()));
        UserPrincipal principal = currentUserService.requirePrincipal();
        if (!principal.canAccessCaseType(caseType.getCode())) {
            throw new ForbiddenException("Not authorized for case type " + caseType.getCode());
        }

        Instant now = Instant.now();
        String actor = principal.getUsername();
        CaseEntity entity = new CaseEntity();
        entity.setCaseNumber(nextCaseNumber());
        entity.setCaseTypeId(caseType.getId());
        entity.setSubject(caseType.getName());
        entity.setCaseOwner(command.caseOwner() == null || command.caseOwner().isBlank()
                ? principal.getDisplayName()
                : command.caseOwner());
        entity.setCaseStatus(command.status() == null || command.status().isBlank() ? "Requested" : command.status());
        entity.setPriority(command.priority() == null || command.priority().isBlank() ? "Medium" : command.priority());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        entity.setVersion(1);

        CaseEntity saved = caseRepository.save(entity);
        enqueueReceivingTeamWork(saved, caseType);
        log.info("case created - caseId={} caseNumber={} userId={}",
                saved.getId(), saved.getCaseNumber(), principal.getUserId());
        return toRef(saved);
    }

    /**
     * After case save: create workflow + notification rows for each receiving team of the case type.
     * Queues remain visible only to users with WF_VIEW/NOTIF_VIEW and matching receiving-team scope.
     */
    private void enqueueReceivingTeamWork(CaseEntity saved, CaseTypeEntity caseType) {
        for (TeamEntity team : caseType.getReceivingTeams()) {
            if (!team.isActive()) {
                continue;
            }
            workflowService.start(new StartWorkflowCommand(
                    saved.getId(),
                    caseType.getCode(),
                    team.getCode(),
                    "Pending Assignment"));
            notificationService.notifyTeam(new NotifyTeamCommand(
                    saved.getId(),
                    team.getCode(),
                    "New case " + saved.getCaseNumber() + " assigned to " + team.getName(),
                    "/cases"));
            log.info(
                    "case save enqueued workflow/notification - caseId={} receivingTeam={}",
                    saved.getId(),
                    team.getCode());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CaseRef getCase(Long caseId) {
        requireCasesView();
        CaseEntity entity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", String.valueOf(caseId)));
        assertCaseTypeAccess(entity.getCaseTypeId());
        return toRef(entity);
    }

    @Override
    @Transactional
    public CaseRef updateCaseHeader(Long caseId, UpdateCaseHeaderCommand command) {
        requireCasesEdit();
        CaseEntity entity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", String.valueOf(caseId)));
        assertCaseTypeAccess(entity.getCaseTypeId());

        if (command.priority() != null && !command.priority().isBlank()) {
            entity.setPriority(command.priority());
        }
        if (command.status() != null && !command.status().isBlank()) {
            entity.setCaseStatus(command.status());
        }
        if (command.assignedTo() != null) {
            entity.setAssignedTo(command.assignedTo());
        }
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(currentUserService.requirePrincipal().getUsername());

        CaseEntity saved = caseRepository.save(entity);
        log.info("case header updated - caseId={} userId={}", saved.getId(), currentUserService.requireUserId());
        return toRef(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaseListItem> listAuthorizedCases() {
        requireCasesView();
        UserPrincipal principal = currentUserService.requirePrincipal();
        List<String> caseTypeCodes = principal.getCaseTypeCodes();
        if (caseTypeCodes.isEmpty()) {
            return List.of();
        }

        List<CaseTypeEntity> types = caseTypeRepository.findByCodeIn(caseTypeCodes);
        if (types.isEmpty()) {
            return List.of();
        }

        Map<Long, CaseTypeEntity> typeById = types.stream()
                .collect(Collectors.toMap(CaseTypeEntity::getId, Function.identity()));
        List<Long> typeIds = List.copyOf(typeById.keySet());

        return caseRepository.findAuthorized(typeIds, false).stream()
                .map(c -> toListItem(c, typeById.get(c.getCaseTypeId())))
                .toList();
    }

    private CaseListItem toListItem(CaseEntity c, CaseTypeEntity type) {
        return new CaseListItem(
                c.getId(),
                c.getCaseNumber(),
                type == null ? "" : type.getName(),
                type == null ? "" : type.getCode(),
                nullToEmpty(c.getClientId()),
                c.getSubject(),
                nullToEmpty(c.getDescription()),
                c.getCaseOwner(),
                c.getCaseStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getRequestedDueDate(),
                nullToEmpty(c.getCarrier()),
                c.getPriority(),
                nullToEmpty(c.getAssignedTo()),
                nullToEmpty(c.getSegmentId()),
                nullToEmpty(c.getFrequency()),
                nullToEmpty(c.getSpokenKey()),
                nullToEmpty(c.getEventId()),
                nullToEmpty(c.getMailMonth()));
    }

    private void assertCaseTypeAccess(Long caseTypeId) {
        CaseTypeEntity type = caseTypeRepository.findById(caseTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("CaseType", String.valueOf(caseTypeId)));
        if (!currentUserService.requirePrincipal().canAccessCaseType(type.getCode())) {
            throw new ForbiddenException("Not authorized for case type " + type.getCode());
        }
    }

    private void requireCasesView() {
        if (!(currentUserService.hasPermission(PermissionCodes.CASES_VIEW)
                || currentUserService.hasPermission(PermissionCodes.CASES_ACCESS))) {
            throw new ForbiddenException("CASES_VIEW required");
        }
    }

    private void requireCasesCreate() {
        if (!(currentUserService.hasPermission(PermissionCodes.CASES_CREATE)
                || currentUserService.hasPermission(PermissionCodes.CASES_ACCESS))) {
            throw new ForbiddenException("CASES_CREATE required");
        }
    }

    private void requireCasesEdit() {
        if (!(currentUserService.hasPermission(PermissionCodes.CASES_EDIT)
                || currentUserService.hasPermission(PermissionCodes.CASES_ACCESS))) {
            throw new ForbiddenException("CASES_EDIT required");
        }
    }

    private String nextCaseNumber() {
        return "CASE-" + (1000 + caseRepository.count() + 1);
    }

    private static CaseRef toRef(CaseEntity entity) {
        long version = entity.getVersion() == null ? 1L : entity.getVersion().longValue();
        return new CaseRef(entity.getId(), entity.getCaseNumber(), version);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
