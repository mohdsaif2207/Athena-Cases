package com.athena.cases.casemanagement;

import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.features.billing.BillingConstants;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.idallocation.BusinessCaseIdService;
import com.athena.cases.notification.NotificationEntity;
import com.athena.cases.notification.NotificationRepository;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import com.athena.cases.workflow.StartWorkflowCommand;
import com.athena.cases.workflow.WorkflowEntity;
import com.athena.cases.workflow.WorkflowRepository;
import com.athena.cases.workflow.WorkflowService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final BusinessCaseIdService businessCaseIdService;
    private final WorkflowRepository workflowRepository;
    private final NotificationRepository notificationRepository;

    public CaseManagementServiceImpl(
            CaseRepository caseRepository,
            CaseTypeRepository caseTypeRepository,
            CurrentUserService currentUserService,
            WorkflowService workflowService,
            NotificationService notificationService,
            BusinessCaseIdService businessCaseIdService,
            WorkflowRepository workflowRepository,
            NotificationRepository notificationRepository) {
        this.caseRepository = caseRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.currentUserService = currentUserService;
        this.workflowService = workflowService;
        this.notificationService = notificationService;
        this.businessCaseIdService = businessCaseIdService;
        this.workflowRepository = workflowRepository;
        this.notificationRepository = notificationRepository;
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
        entity.setCaseNumber(resolveDisplayCaseNumber(caseType.getCode()));
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
     * Initiating teams are never queued — only {@link CaseTypeEntity#getReceivingTeams()}.
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
                    notificationMessage(caseType, saved.getCaseNumber(), team.getName()),
                    notificationDeepLink(caseType, saved.getId())));
            log.info(
                    "case save enqueued workflow/notification - caseId={} receivingTeam={}",
                    saved.getId(),
                    team.getCode());
        }
    }

    private static String notificationMessage(CaseTypeEntity caseType, String caseNumber, String teamName) {
        if (BillingConstants.CASE_TYPE_CODE.equals(caseType.getCode())) {
            return BillingConstants.newCaseNotificationMessage(caseNumber);
        }
        return "New case " + caseNumber + " assigned to " + teamName;
    }

    private static String notificationDeepLink(CaseTypeEntity caseType, Long caseId) {
        if (BillingConstants.CASE_TYPE_CODE.equals(caseType.getCode())) {
            return BillingConstants.caseDeepLink(caseId);
        }
        return "/cases";
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
        CaseTypeEntity caseType = caseTypeRepository.findById(entity.getCaseTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("CaseType", String.valueOf(entity.getCaseTypeId())));
        assertCaseTypeAccess(entity.getCaseTypeId());
        assertReceivingTeamEditAccess(caseType);

        if (command.priority() != null && !command.priority().isBlank()) {
            entity.setPriority(command.priority());
        }
        if (command.status() != null && !command.status().isBlank()) {
            entity.setCaseStatus(command.status());
        }
        if (command.assignedTo() != null) {
            entity.setAssignedTo(command.assignedTo());
        }
        if (command.subject() != null && !command.subject().isBlank()) {
            entity.setSubject(command.subject().trim());
        }
        if (command.description() != null) {
            entity.setDescription(command.description().trim());
        }
        if (command.clientId() != null) {
            entity.setClientId(command.clientId().trim());
        }
        if (command.requestedDueDate() != null) {
            entity.setRequestedDueDate(command.requestedDueDate());
        }
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(currentUserService.requirePrincipal().getUsername());

        CaseEntity saved = caseRepository.save(entity);
        syncQueuesFromCase(saved);
        log.info("case header updated - caseId={} userId={}", saved.getId(), currentUserService.requireUserId());
        return toRef(saved);
    }

    /**
     * Reflect case header changes onto related workflow / notification queue rows.
     */
    private void syncQueuesFromCase(CaseEntity saved) {
        Instant now = Instant.now();
        String actor = currentUserService.requirePrincipal().getUsername();

        List<WorkflowEntity> workflows = workflowRepository.findByCaseIdOrderByReceivedAtDesc(saved.getId());
        for (WorkflowEntity w : workflows) {
            if (saved.getCaseStatus() != null && !saved.getCaseStatus().isBlank()) {
                w.setStatus(saved.getCaseStatus());
            }
            if (saved.getPriority() != null && !saved.getPriority().isBlank()) {
                w.setPriority(saved.getPriority());
            }
            if (saved.getAssignedTo() != null && !saved.getAssignedTo().isBlank()) {
                w.setOwnerName(saved.getAssignedTo());
            }
            if (saved.getSubject() != null && !saved.getSubject().isBlank()) {
                w.setMessageName(saved.getSubject() + " Workflow");
            }
            w.setUpdatedAt(now);
            w.setUpdatedBy(actor);
        }
        if (!workflows.isEmpty()) {
            workflowRepository.saveAll(workflows);
        }

        List<NotificationEntity> notifications =
                notificationRepository.findByCaseIdOrderByReceivedAtDesc(saved.getId());
        for (NotificationEntity n : notifications) {
            if (saved.getSubject() != null && !saved.getSubject().isBlank()) {
                n.setMessageName(saved.getSubject() + " Notice");
            }
            n.setMessage("Case " + saved.getCaseNumber() + " updated — status: "
                    + nullToEmpty(saved.getCaseStatus()) + ", priority: "
                    + nullToEmpty(saved.getPriority()));
            n.setUpdatedAt(now);
            n.setUpdatedBy(actor);
        }
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    /** Receiving-team case users may edit only when they belong to a receiving team of the case type. */
    private void assertReceivingTeamEditAccess(CaseTypeEntity caseType) {
        UserPrincipal principal = currentUserService.requirePrincipal();
        if (principal.getPermissionCodes().contains(PermissionCodes.ADMIN_ACCESS)
                || principal.getRoleCodes().contains("SYSTEM_ADMINISTRATOR")) {
            return;
        }
        Set<String> userTeams = Set.copyOf(principal.getReceivingTeamCodes());
        boolean onReceivingTeam = caseType.getReceivingTeams().stream()
                .filter(TeamEntity::isActive)
                .map(TeamEntity::getCode)
                .anyMatch(userTeams::contains);
        if (!onReceivingTeam) {
            throw new ForbiddenException(
                    "Cases Edit requires membership on a receiving team for case type " + caseType.getCode());
        }
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

    /**
     * Prefer shared prefix sequences (BIL###### / DBM###### / …) when registered for the case type.
     * Falls back to legacy {@code CASE-xxxx} so unregistered types keep working.
     */
    private String resolveDisplayCaseNumber(String caseTypeCode) {
        try {
            return businessCaseIdService.allocate(caseTypeCode);
        } catch (ResourceNotFoundException ex) {
            log.debug("no display-id sequence for caseTypeCode={}; using CASE- fallback", caseTypeCode);
            return nextCaseNumber();
        }
    }

    private static CaseRef toRef(CaseEntity entity) {
        long version = entity.getVersion() == null ? 1L : entity.getVersion().longValue();
        return new CaseRef(entity.getId(), entity.getCaseNumber(), version);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
