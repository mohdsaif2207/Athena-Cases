package com.athena.cases.features.dbm.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.enums.CaseTypeCode;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.features.dbm.DbmConstants;
import com.athena.cases.features.dbm.dto.CreateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.dto.DbmWorkOrderResponse;
import com.athena.cases.features.dbm.dto.UpdateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.entity.DbmWorkOrder;
import com.athena.cases.features.dbm.entity.DbmWorkOrderAccountType;
import com.athena.cases.features.dbm.entity.DbmWorkOrderCoverageLevel;
import com.athena.cases.features.dbm.entity.DbmWorkOrderSpokenKey;
import com.athena.cases.features.dbm.repository.DbmWorkOrderRepository;
import com.athena.cases.features.dbm.service.DbmWorkOrderService;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.workflow.StartWorkflowCommand;
import com.athena.cases.workflow.WorkflowService;

/**
 * DBM Work Order Request service.
 * Create path persists Case + DBM detail + multi-select children,
 * then starts shared workflow and notifies the DBM receiving team.
 */
@Service
@Transactional
public class DbmWorkOrderServiceImpl implements DbmWorkOrderService {

    private static final Logger log = LoggerFactory.getLogger(DbmWorkOrderServiceImpl.class);

    private static final String CASE_NUMBER_PREFIX = "DBM";
    private static final int CASE_NUMBER_WIDTH = 6;
    private static final int MAX_CASE_NUMBER_SEQUENCE = 999_999;
    private static final String DEFAULT_FREQUENCY = "Once";

    private final CaseRepository caseRepository;
    private final CaseTypeRepository caseTypeRepository;
    private final DbmWorkOrderRepository dbmWorkOrderRepository;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    public DbmWorkOrderServiceImpl(
            CaseRepository caseRepository,
            CaseTypeRepository caseTypeRepository,
            DbmWorkOrderRepository dbmWorkOrderRepository,
            WorkflowService workflowService,
            NotificationService notificationService,
            CurrentUserService currentUserService) {
        this.caseRepository = caseRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.dbmWorkOrderRepository = dbmWorkOrderRepository;
        this.workflowService = workflowService;
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @Override
    public DbmWorkOrderResponse create(CreateDbmWorkOrderRequest request) {
        requirePermission(PermissionCodes.CASES_CREATE);

        Instant now = Instant.now();
        String actor = resolveActorDisplayName();
        boolean dbmUser = isDbmReceivingUser();

        CaseTypeEntity caseType = caseTypeRepository
                .findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name())
                .orElseThrow(() -> new IllegalStateException(
                        "Case type not seeded: " + CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()));

        CaseEntity savedCase = persistNewCaseWithUniqueNumber(request, caseType, actor, now);

        DbmWorkOrder dbmWorkOrder = new DbmWorkOrder();
        dbmWorkOrder.setCaseEntity(savedCase);
        mapRequestToDbmWorkOrder(request, dbmWorkOrder, dbmUser, null);
        dbmWorkOrder.setCreatedAt(now);
        dbmWorkOrder.setUpdatedAt(now);
        dbmWorkOrder.setCreatedBy(actor);
        dbmWorkOrder.setUpdatedBy(actor);

        addCoverageLevels(dbmWorkOrder, request.coverageLevels(), now, actor);
        addAccountTypes(dbmWorkOrder, request.requestedAccountTypes(), now, actor);
        addSpokenKeys(dbmWorkOrder, request.spokenKeys(), now, actor);

        DbmWorkOrder savedDbm = dbmWorkOrderRepository.saveAndFlush(dbmWorkOrder);

        enqueueReceivingTeamWork(savedCase, caseType);

        log.info(
                "dbm work order created - caseId={} caseNumber={} owner={} pendingApproval={}",
                savedCase.getId(),
                savedCase.getCaseNumber(),
                savedCase.getCaseOwner(),
                savedCase.isPendingDbmApproval());

        return toResponse(savedCase, savedDbm, caseType.getCode(), dbmUser);
    }

    private String resolveActorDisplayName() {
        String displayName = currentUserService.requireDisplayName();
        if (displayName == null || displayName.isBlank()) {
            return currentUserService.requirePrincipal().getUsername();
        }
        return displayName.trim();
    }

    /**
     * Allocates the next DBM###### from MAX(existing) and flushes so the row is durable
     * before DBM detail / workflow / notification inserts.
     */
    private CaseEntity persistNewCaseWithUniqueNumber(
            CreateDbmWorkOrderRequest request,
            CaseTypeEntity caseType,
            String actor,
            Instant now) {
        String caseNumber = generateNextDbmCaseNumber();
        CaseEntity caseEntity = new CaseEntity();
        caseEntity.setCaseNumber(caseNumber);
        caseEntity.setCaseTypeId(caseType.getId());
        caseEntity.setSubject(request.subject());
        caseEntity.setDescription(request.description());
        caseEntity.setCaseStatus(request.status());
        caseEntity.setPriority(request.priority());
        caseEntity.setCaseOwner(actor);
        caseEntity.setRequestedDueDate(request.requestedDueDate());
        caseEntity.setClientId(request.clientId());
        caseEntity.setEventId(request.eventId());
        caseEntity.setMailMonth(request.mailMonth());
        caseEntity.setFrequency(
                request.frequency() == null || request.frequency().isBlank()
                        ? DEFAULT_FREQUENCY
                        : request.frequency());
        caseEntity.setPendingDbmApproval(isCustomTransferType(request.transferType()));
        caseEntity.setCreatedAt(now);
        caseEntity.setUpdatedAt(now);
        caseEntity.setCreatedBy(actor);
        caseEntity.setUpdatedBy(actor);
        caseEntity.setVersion(1);

        return caseRepository.saveAndFlush(caseEntity);
    }

    /**
     * Starts workflow + notification for each active receiving team mapped to the case type.
     * Uses seeded team codes (e.g. {@code DBM_TEAM}), not display names.
     */
    private void enqueueReceivingTeamWork(CaseEntity savedCase, CaseTypeEntity caseType) {
        Long caseId = savedCase.getId();
        for (TeamEntity team : caseType.getReceivingTeams()) {
            if (!team.isActive()) {
                continue;
            }
            workflowService.start(new StartWorkflowCommand(
                    caseId,
                    caseType.getCode(),
                    team.getCode(),
                    DbmConstants.WORKFLOW_STATUS_PENDING_ASSIGNMENT));
            notificationService.notifyTeam(new NotifyTeamCommand(
                    caseId,
                    team.getCode(),
                    DbmConstants.newCaseNotificationMessage(savedCase.getCaseNumber()),
                    DbmConstants.caseDeepLink(caseId)));
        }
    }

    @Override
    public DbmWorkOrderResponse update(Long caseId, UpdateDbmWorkOrderRequest request) {
        requirePermission(PermissionCodes.CASES_EDIT);
        boolean dbmUser = isDbmReceivingUser();

        Instant now = Instant.now();
        String actor = resolveActorDisplayName();

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", String.valueOf(caseId)));

        DbmWorkOrder dbmWorkOrder = dbmWorkOrderRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DBM Work Order", String.valueOf(caseId)));

        caseEntity.setSubject(request.subject());
        caseEntity.setDescription(request.description());
        caseEntity.setCaseStatus(request.status());
        caseEntity.setPriority(request.priority());
        caseEntity.setRequestedDueDate(request.requestedDueDate());
        caseEntity.setClientId(request.clientId());
        caseEntity.setEventId(request.eventId());
        caseEntity.setMailMonth(request.mailMonth());
        caseEntity.setPendingDbmApproval(isCustomTransferType(request.transferType()));
        caseEntity.setUpdatedAt(now);
        caseEntity.setUpdatedBy(actor);

        mapRequestToDbmWorkOrder(request, dbmWorkOrder, dbmUser, dbmWorkOrder);
        dbmWorkOrder.setUpdatedAt(now);
        dbmWorkOrder.setUpdatedBy(actor);

        dbmWorkOrder.getCoverageLevels().clear();
        dbmWorkOrder.getAccountTypes().clear();
        dbmWorkOrder.getSpokenKeys().clear();
        addCoverageLevels(dbmWorkOrder, request.coverageLevels(), now, actor);
        addAccountTypes(dbmWorkOrder, request.requestedAccountTypes(), now, actor);
        addSpokenKeys(dbmWorkOrder, request.spokenKeys(), now, actor);

        CaseEntity savedCase = caseRepository.save(caseEntity);
        DbmWorkOrder savedDbm = dbmWorkOrderRepository.save(dbmWorkOrder);

        CaseTypeEntity caseType = caseTypeRepository.findById(savedCase.getCaseTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CaseType", String.valueOf(savedCase.getCaseTypeId())));
        notifyReceivingTeamsOfUpdate(savedCase, caseType);

        log.info(
                "dbm work order updated - caseId={} caseNumber={}",
                savedCase.getId(),
                savedCase.getCaseNumber());

        return toResponse(savedCase, savedDbm, caseType.getCode(), dbmUser);
    }

    /**
     * Notification only on edit (no second workflow instance).
     */
    private void notifyReceivingTeamsOfUpdate(CaseEntity savedCase, CaseTypeEntity caseType) {
        Long caseId = savedCase.getId();
        for (TeamEntity team : caseType.getReceivingTeams()) {
            if (!team.isActive()) {
                continue;
            }
            notificationService.notifyTeam(new NotifyTeamCommand(
                    caseId,
                    team.getCode(),
                    DbmConstants.updatedCaseNotificationMessage(savedCase.getCaseNumber()),
                    DbmConstants.caseDeepLink(caseId)));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DbmWorkOrderResponse getByCaseId(Long caseId) {
        requirePermission(PermissionCodes.CASES_VIEW);
        boolean dbmUser = isDbmReceivingUser();

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", String.valueOf(caseId)));

        DbmWorkOrder dbmWorkOrder = dbmWorkOrderRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DBM Work Order", String.valueOf(caseId)));

        String caseTypeCode = caseTypeRepository.findById(caseEntity.getCaseTypeId())
                .map(CaseTypeEntity::getCode)
                .orElse(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name());

        return toResponse(caseEntity, dbmWorkOrder, caseTypeCode, dbmUser);
    }

    /**
     * Next DBM case number from MAX(existing) + 1. Zero-padded so lexical MAX matches numeric order.
     */
    private String generateNextDbmCaseNumber() {
        int nextSequence = caseRepository.findMaxDbmCaseNumber()
                .map(DbmWorkOrderServiceImpl::parseDbmSequence)
                .map(seq -> seq + 1)
                .orElse(1);
        if (nextSequence > MAX_CASE_NUMBER_SEQUENCE) {
            throw new IllegalStateException("Exhausted DBM case number sequence space");
        }
        return CASE_NUMBER_PREFIX + String.format("%0" + CASE_NUMBER_WIDTH + "d", nextSequence);
    }

    private static int parseDbmSequence(String caseNumber) {
        if (caseNumber == null || caseNumber.length() <= CASE_NUMBER_PREFIX.length()) {
            return 0;
        }
        String suffix = caseNumber.substring(CASE_NUMBER_PREFIX.length());
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static boolean isCustomTransferType(String transferType) {
        if (transferType == null) {
            return false;
        }
        String normalized = transferType.trim();
        return DbmConstants.TRANSFER_TYPE_CUSTOM.equalsIgnoreCase(normalized)
                || DbmConstants.TRANSFER_TYPE_CUSTOM_REQUIRES_APPROVAL.equalsIgnoreCase(normalized);
    }

    private void mapRequestToDbmWorkOrder(
            CreateDbmWorkOrderRequest request,
            DbmWorkOrder target,
            boolean dbmUser,
            DbmWorkOrder existing) {
        applyDbmWorkOrderFields(
                target,
                request.vendor(),
                request.coreProcessorConversion(),
                request.transferType(),
                request.returnFileExpected(),
                request.pgpKeyAtAcxiom(),
                request.expectedQuantity(),
                request.frequency(),
                request.specialInstructions(),
                request.eventId(),
                request.mediaIds(),
                request.mailMonth(),
                request.mediaOutQuantity(),
                request.changesToMatchbackDb(),
                request.selectionCriteria(),
                request.matchbackField(),
                request.changeTo(),
                resolveDbmOnlyText(dbmUser, request.dbmWorkOrderNumber(), existing == null ? null : existing.getDbmWorkOrderNumber()),
                resolveDbmOnlyText(dbmUser, request.dbmCompletionNotes(), existing == null ? null : existing.getDbmCompletionNotes()),
                resolveDbmOnlyInt(dbmUser, request.totalRecordsUpdated(), existing == null ? null : existing.getTotalRecordsUpdated()));
    }

    private void mapRequestToDbmWorkOrder(
            UpdateDbmWorkOrderRequest request,
            DbmWorkOrder target,
            boolean dbmUser,
            DbmWorkOrder existing) {
        applyDbmWorkOrderFields(
                target,
                request.vendor(),
                request.coreProcessorConversion(),
                request.transferType(),
                request.returnFileExpected(),
                request.pgpKeyAtAcxiom(),
                request.expectedQuantity(),
                request.frequency(),
                request.specialInstructions(),
                request.eventId(),
                request.mediaIds(),
                request.mailMonth(),
                request.mediaOutQuantity(),
                request.changesToMatchbackDb(),
                request.selectionCriteria(),
                request.matchbackField(),
                request.changeTo(),
                resolveDbmOnlyText(dbmUser, request.dbmWorkOrderNumber(), existing.getDbmWorkOrderNumber()),
                resolveDbmOnlyText(dbmUser, request.dbmCompletionNotes(), existing.getDbmCompletionNotes()),
                resolveDbmOnlyInt(dbmUser, request.totalRecordsUpdated(), existing.getTotalRecordsUpdated()));
    }

    /** Non-DBM users cannot set Section 4 fields; keep existing values on update. */
    private static String resolveDbmOnlyText(boolean dbmUser, String requested, String existing) {
        return dbmUser ? requested : existing;
    }

    private static Integer resolveDbmOnlyInt(boolean dbmUser, Integer requested, Integer existing) {
        return dbmUser ? requested : existing;
    }

    private void applyDbmWorkOrderFields(
            DbmWorkOrder target,
            String vendor,
            boolean coreProcessorConversion,
            String transferType,
            String returnFileExpected,
            String pgpKeyAtAcxiom,
            Integer expectedQuantity,
            String frequency,
            String specialInstructions,
            String eventId,
            String mediaIds,
            String mailMonth,
            Integer mediaOutQuantity,
            boolean changesToMatchbackDb,
            String selectionCriteria,
            String matchbackField,
            String changeTo,
            String dbmWorkOrderNumber,
            String dbmCompletionNotes,
            Integer totalRecordsUpdated) {
        target.setVendor(vendor);
        target.setCoreProcessorConversion(coreProcessorConversion);
        target.setTransferType(transferType);
        target.setReturnFileExpected(returnFileExpected);
        target.setPgpKeyAtAcxiom(pgpKeyAtAcxiom);
        target.setExpectedQuantity(expectedQuantity);
        target.setFrequency(
                frequency == null || frequency.isBlank() ? DEFAULT_FREQUENCY : frequency);
        target.setSpecialInstructions(specialInstructions);
        target.setEventId(eventId);
        target.setMediaIds(mediaIds);
        target.setMailMonth(mailMonth);
        target.setMediaOutQuantity(mediaOutQuantity);
        target.setChangesToMatchbackDb(changesToMatchbackDb);
        target.setSelectionCriteria(selectionCriteria);
        target.setMatchbackField(matchbackField);
        target.setChangeTo(changeTo);
        target.setDbmWorkOrderNumber(dbmWorkOrderNumber);
        target.setDbmCompletionNotes(dbmCompletionNotes);
        target.setTotalRecordsUpdated(totalRecordsUpdated);
    }

    private void addCoverageLevels(DbmWorkOrder parent, List<String> values, Instant now, String actor) {
        for (String value : distinctNonBlank(values)) {
            DbmWorkOrderCoverageLevel row = new DbmWorkOrderCoverageLevel();
            row.setDbmWorkOrder(parent);
            row.setCoverageLevel(value);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            row.setCreatedBy(actor);
            row.setUpdatedBy(actor);
            parent.getCoverageLevels().add(row);
        }
    }

    private void addAccountTypes(DbmWorkOrder parent, List<String> values, Instant now, String actor) {
        for (String value : distinctNonBlank(values)) {
            DbmWorkOrderAccountType row = new DbmWorkOrderAccountType();
            row.setDbmWorkOrder(parent);
            row.setAccountType(value);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            row.setCreatedBy(actor);
            row.setUpdatedBy(actor);
            parent.getAccountTypes().add(row);
        }
    }

    private void addSpokenKeys(DbmWorkOrder parent, List<String> values, Instant now, String actor) {
        for (String value : distinctNonBlank(values)) {
            DbmWorkOrderSpokenKey row = new DbmWorkOrderSpokenKey();
            row.setDbmWorkOrder(parent);
            row.setSpokenKey(value);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            row.setCreatedBy(actor);
            row.setUpdatedBy(actor);
            parent.getSpokenKeys().add(row);
        }
    }

    private static List<String> distinctNonBlank(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> distinct = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                distinct.add(value.trim());
            }
        }
        return new ArrayList<>(distinct);
    }

    private DbmWorkOrderResponse toResponse(
            CaseEntity caseEntity,
            DbmWorkOrder dbmWorkOrder,
            String caseTypeCode,
            boolean dbmUser) {
        return new DbmWorkOrderResponse(
                caseEntity.getId(),
                caseEntity.getCaseNumber(),
                caseTypeCode,
                caseEntity.getCaseOwner(),
                caseEntity.getRequestedDueDate(),
                caseEntity.getPriority(),
                caseEntity.getSubject(),
                caseEntity.getCaseStatus(),
                caseEntity.getDescription(),
                caseEntity.getClientId(),
                caseEntity.isPendingDbmApproval(),
                dbmWorkOrder.getVendor(),
                dbmWorkOrder.isCoreProcessorConversion(),
                dbmWorkOrder.getTransferType(),
                dbmWorkOrder.getCoverageLevels().stream()
                        .map(DbmWorkOrderCoverageLevel::getCoverageLevel)
                        .toList(),
                dbmWorkOrder.getReturnFileExpected(),
                dbmWorkOrder.getPgpKeyAtAcxiom(),
                dbmWorkOrder.getAccountTypes().stream()
                        .map(DbmWorkOrderAccountType::getAccountType)
                        .toList(),
                dbmWorkOrder.getExpectedQuantity(),
                dbmWorkOrder.getFrequency(),
                dbmWorkOrder.getSpecialInstructions(),
                dbmWorkOrder.getSpokenKeys().stream()
                        .map(DbmWorkOrderSpokenKey::getSpokenKey)
                        .toList(),
                dbmWorkOrder.getEventId(),
                dbmWorkOrder.getMediaIds(),
                dbmWorkOrder.getMailMonth(),
                dbmWorkOrder.getMediaOutQuantity(),
                dbmWorkOrder.isChangesToMatchbackDb(),
                dbmWorkOrder.getSelectionCriteria(),
                dbmWorkOrder.getMatchbackField(),
                dbmWorkOrder.getChangeTo(),
                dbmUser ? dbmWorkOrder.getDbmWorkOrderNumber() : null,
                dbmUser ? dbmWorkOrder.getDbmCompletionNotes() : null,
                dbmUser ? dbmWorkOrder.getTotalRecordsUpdated() : null,
                dbmWorkOrder.getCreatedAt(),
                dbmWorkOrder.getUpdatedAt()
        );
    }

    private void requirePermission(String permissionCode) {
        if (!currentUserService.hasPermission(permissionCode)) {
            throw new ForbiddenException(permissionCode + " required");
        }
    }

    /** AC8 — Section 4 is for users on the DBM receiving team only. */
    private boolean isDbmReceivingUser() {
        return currentUserService.requirePrincipal().getReceivingTeamCodes().stream()
                .anyMatch(code -> DbmConstants.RECEIVER_TEAM_DBM.equalsIgnoreCase(code));
    }
}
