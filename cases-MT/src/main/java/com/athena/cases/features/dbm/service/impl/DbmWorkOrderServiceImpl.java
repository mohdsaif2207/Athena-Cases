package com.athena.cases.features.dbm.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.enums.CaseTypeCode;
import com.athena.cases.features.dbm.dto.CreateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.dto.DbmWorkOrderResponse;
import com.athena.cases.features.dbm.dto.UpdateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.entity.DbmWorkOrder;
import com.athena.cases.features.dbm.entity.DbmWorkOrderAccountType;
import com.athena.cases.features.dbm.entity.DbmWorkOrderCoverageLevel;
import com.athena.cases.features.dbm.entity.DbmWorkOrderSpokenKey;
import com.athena.cases.features.dbm.repository.DbmWorkOrderAccountTypeRepository;
import com.athena.cases.features.dbm.repository.DbmWorkOrderCoverageLevelRepository;
import com.athena.cases.features.dbm.repository.DbmWorkOrderRepository;
import com.athena.cases.features.dbm.repository.DbmWorkOrderSpokenKeyRepository;
import com.athena.cases.features.dbm.service.DbmWorkOrderService;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
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

    private static final String CASE_NUMBER_PREFIX = "DBM";
    private static final int CASE_NUMBER_WIDTH = 6;
    private static final int MAX_CASE_NUMBER_SEQUENCE = 999_999;
    private static final String DEFAULT_FREQUENCY = "Once";
    private static final String TRANSFER_TYPE_CUSTOM = "Custom";
    private static final String TRANSFER_TYPE_CUSTOM_REQUIRES_APPROVAL = "Custom (Requires Approval)";

    /**
     * Temporary actor until {@code CurrentUserService} has a Spring bean implementation.
     * TODO(auth): replace with CurrentUserService.requireDisplayName() / requireUserId().
     */
    private static final String TEMP_CASE_OWNER = "SYSTEM";

    private final CaseRepository caseRepository;
    private final CaseTypeRepository caseTypeRepository;
    private final DbmWorkOrderRepository dbmWorkOrderRepository;
    private final DbmWorkOrderCoverageLevelRepository dbmWorkOrderCoverageLevelRepository;
    private final DbmWorkOrderAccountTypeRepository dbmWorkOrderAccountTypeRepository;
    private final DbmWorkOrderSpokenKeyRepository dbmWorkOrderSpokenKeyRepository;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;

    public DbmWorkOrderServiceImpl(
            CaseRepository caseRepository,
            CaseTypeRepository caseTypeRepository,
            DbmWorkOrderRepository dbmWorkOrderRepository,
            DbmWorkOrderCoverageLevelRepository dbmWorkOrderCoverageLevelRepository,
            DbmWorkOrderAccountTypeRepository dbmWorkOrderAccountTypeRepository,
            DbmWorkOrderSpokenKeyRepository dbmWorkOrderSpokenKeyRepository,
            WorkflowService workflowService,
            NotificationService notificationService) {
        this.caseRepository = caseRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.dbmWorkOrderRepository = dbmWorkOrderRepository;
        this.dbmWorkOrderCoverageLevelRepository = dbmWorkOrderCoverageLevelRepository;
        this.dbmWorkOrderAccountTypeRepository = dbmWorkOrderAccountTypeRepository;
        this.dbmWorkOrderSpokenKeyRepository = dbmWorkOrderSpokenKeyRepository;
        this.workflowService = workflowService;
        this.notificationService = notificationService;
    }

    @Override
    public DbmWorkOrderResponse create(CreateDbmWorkOrderRequest request) {
        Instant now = Instant.now();
        String actor = TEMP_CASE_OWNER;

        CaseTypeEntity caseType = caseTypeRepository
                .findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name())
                .orElseThrow(() -> new IllegalStateException(
                        "Case type not seeded: " + CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()));

        CaseEntity caseEntity = new CaseEntity();
        caseEntity.setCaseNumber(generateNextDbmCaseNumber());
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
        caseEntity.setCreatedAt(now);
        caseEntity.setUpdatedAt(now);
        caseEntity.setCreatedBy(actor);
        caseEntity.setUpdatedBy(actor);
        caseEntity.setVersion(1);

        CaseEntity savedCase = caseRepository.save(caseEntity);

        DbmWorkOrder dbmWorkOrder = new DbmWorkOrder();
        dbmWorkOrder.setCaseEntity(savedCase);
        mapRequestToDbmWorkOrder(request, dbmWorkOrder);
        dbmWorkOrder.setCreatedAt(now);
        dbmWorkOrder.setUpdatedAt(now);
        dbmWorkOrder.setCreatedBy(actor);
        dbmWorkOrder.setUpdatedBy(actor);

        addCoverageLevels(dbmWorkOrder, request.coverageLevels(), now, actor);
        addAccountTypes(dbmWorkOrder, request.requestedAccountTypes(), now, actor);
        addSpokenKeys(dbmWorkOrder, request.spokenKeys(), now, actor);

        DbmWorkOrder savedDbm = dbmWorkOrderRepository.save(dbmWorkOrder);

        enqueueReceivingTeamWork(savedCase, caseType);

        return toResponse(savedCase, savedDbm, caseType.getCode());
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
                    "Pending Assignment"));
            notificationService.notifyTeam(new NotifyTeamCommand(
                    caseId,
                    team.getCode(),
                    "A new DBM Work Order Request (Case " + savedCase.getCaseNumber()
                            + ") has been assigned to your team.",
                    "/cases/" + caseId));
        }
    }

    @Override
    public DbmWorkOrderResponse update(Long caseId, UpdateDbmWorkOrderRequest request) {
        Instant now = Instant.now();
        String actor = TEMP_CASE_OWNER;

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalStateException("Case not found: " + caseId));

        DbmWorkOrder dbmWorkOrder = dbmWorkOrderRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalStateException(
                        "DBM Work Order not found for caseId: " + caseId));

        caseEntity.setSubject(request.subject());
        caseEntity.setDescription(request.description());
        caseEntity.setCaseStatus(request.status());
        caseEntity.setPriority(request.priority());
        caseEntity.setRequestedDueDate(request.requestedDueDate());
        caseEntity.setClientId(request.clientId());
        caseEntity.setEventId(request.eventId());
        caseEntity.setMailMonth(request.mailMonth());
        caseEntity.setUpdatedAt(now);
        caseEntity.setUpdatedBy(actor);

        mapRequestToDbmWorkOrder(request, dbmWorkOrder);
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

        String caseTypeCode = caseTypeRepository.findById(savedCase.getCaseTypeId())
                .map(CaseTypeEntity::getCode)
                .orElse(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name());

        return toResponse(savedCase, savedDbm, caseTypeCode);
    }

    @Override
    @Transactional(readOnly = true)
    public DbmWorkOrderResponse getByCaseId(Long caseId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalStateException("Case not found: " + caseId));

        DbmWorkOrder dbmWorkOrder = dbmWorkOrderRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalStateException(
                        "DBM Work Order not found for caseId: " + caseId));

        String caseTypeCode = caseTypeRepository.findById(caseEntity.getCaseTypeId())
                .map(CaseTypeEntity::getCode)
                .orElse(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name());

        return toResponse(caseEntity, dbmWorkOrder, caseTypeCode);
    }

    /**
     * Temporary unique Case Number generator.
     * TODO(case-number): replace with a DB sequence / allocated counter for concurrency safety.
     */
    private String generateNextDbmCaseNumber() {
        for (int sequence = 1; sequence <= MAX_CASE_NUMBER_SEQUENCE; sequence++) {
            String candidate = CASE_NUMBER_PREFIX + String.format("%0" + CASE_NUMBER_WIDTH + "d", sequence);
            if (caseRepository.findByCaseNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Exhausted DBM case number sequence space");
    }

    private static boolean isCustomTransferType(String transferType) {
        if (transferType == null) {
            return false;
        }
        String normalized = transferType.trim();
        return TRANSFER_TYPE_CUSTOM.equalsIgnoreCase(normalized)
                || TRANSFER_TYPE_CUSTOM_REQUIRES_APPROVAL.equalsIgnoreCase(normalized);
    }

    private void mapRequestToDbmWorkOrder(CreateDbmWorkOrderRequest request, DbmWorkOrder target) {
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
                request.dbmWorkOrderNumber(),
                request.dbmCompletionNotes(),
                request.totalRecordsUpdated());
    }

    private void mapRequestToDbmWorkOrder(UpdateDbmWorkOrderRequest request, DbmWorkOrder target) {
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
                request.dbmWorkOrderNumber(),
                request.dbmCompletionNotes(),
                request.totalRecordsUpdated());
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
            String caseTypeCode) {
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
                isCustomTransferType(dbmWorkOrder.getTransferType()),
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
                dbmWorkOrder.getDbmWorkOrderNumber(),
                dbmWorkOrder.getDbmCompletionNotes(),
                dbmWorkOrder.getTotalRecordsUpdated(),
                dbmWorkOrder.getCreatedAt(),
                dbmWorkOrder.getUpdatedAt()
        );
    }
}
