package com.athena.cases.features.dbm.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athena.cases.casemanagement.entity.Case;
import com.athena.cases.casemanagement.repository.CaseRepository;
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

/**
 * DBM Work Order Request service.
 * Create path persists Case + DBM detail + multi-select children.
 * Workflow and notification remain deferred.
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
    private final DbmWorkOrderRepository dbmWorkOrderRepository;
    private final DbmWorkOrderCoverageLevelRepository dbmWorkOrderCoverageLevelRepository;
    private final DbmWorkOrderAccountTypeRepository dbmWorkOrderAccountTypeRepository;
    private final DbmWorkOrderSpokenKeyRepository dbmWorkOrderSpokenKeyRepository;

    public DbmWorkOrderServiceImpl(
            CaseRepository caseRepository,
            DbmWorkOrderRepository dbmWorkOrderRepository,
            DbmWorkOrderCoverageLevelRepository dbmWorkOrderCoverageLevelRepository,
            DbmWorkOrderAccountTypeRepository dbmWorkOrderAccountTypeRepository,
            DbmWorkOrderSpokenKeyRepository dbmWorkOrderSpokenKeyRepository) {
        this.caseRepository = caseRepository;
        this.dbmWorkOrderRepository = dbmWorkOrderRepository;
        this.dbmWorkOrderCoverageLevelRepository = dbmWorkOrderCoverageLevelRepository;
        this.dbmWorkOrderAccountTypeRepository = dbmWorkOrderAccountTypeRepository;
        this.dbmWorkOrderSpokenKeyRepository = dbmWorkOrderSpokenKeyRepository;
    }

    @Override
    public DbmWorkOrderResponse create(CreateDbmWorkOrderRequest request) {
        Instant now = Instant.now();
        String actor = TEMP_CASE_OWNER;

        Case caseEntity = new Case();
        caseEntity.setCaseNumber(generateNextDbmCaseNumber());
        caseEntity.setCaseType(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name());
        caseEntity.setSubject(request.subject());
        caseEntity.setDescription(request.description());
        caseEntity.setStatus(request.status());
        caseEntity.setPriority(request.priority());
        caseEntity.setCaseOwner(actor);
        caseEntity.setRequestedDueDate(request.requestedDueDate());
        caseEntity.setClientId(request.clientId());
        caseEntity.setPendingDbmApproval(isCustomTransferType(request.transferType()));
        caseEntity.setCreatedAt(now);
        caseEntity.setUpdatedAt(now);
        caseEntity.setCreatedBy(actor);
        caseEntity.setUpdatedBy(actor);

        Case savedCase = caseRepository.save(caseEntity);

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

        // TODO trigger workflow (Pending Assignment, assigned_team=DBM)
        // TODO trigger notification for DBM receiving team

        return toResponse(savedCase, savedDbm);
    }

    @Override
    public DbmWorkOrderResponse update(Long caseId, UpdateDbmWorkOrderRequest request) {
        // TODO load existing Case and DbmWorkOrder
        // TODO update shared Case header fields
        // TODO update DbmWorkOrder fields
        // TODO replace Coverage Levels
        // TODO replace Account Types
        // TODO replace Spoken Keys
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public DbmWorkOrderResponse getByCaseId(Long caseId) {
        // TODO load Case by id
        // TODO load DbmWorkOrder by caseId
        // TODO load Coverage Levels / Account Types / Spoken Keys as needed
        // TODO map to DbmWorkOrderResponse
        return null;
    }

    /**
     * Temporary unique Case Number generator using {@link CaseRepository#existsByCaseNumber(String)}.
     * TODO(case-number): replace with a DB sequence / allocated counter for concurrency safety.
     */
    private String generateNextDbmCaseNumber() {
        for (int sequence = 1; sequence <= MAX_CASE_NUMBER_SEQUENCE; sequence++) {
            String candidate = CASE_NUMBER_PREFIX + String.format("%0" + CASE_NUMBER_WIDTH + "d", sequence);
            if (!caseRepository.existsByCaseNumber(candidate)) {
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
        target.setVendor(request.vendor());
        target.setCoreProcessorConversion(request.coreProcessorConversion());
        target.setTransferType(request.transferType());
        target.setReturnFileExpected(request.returnFileExpected());
        target.setPgpKeyAtAcxiom(request.pgpKeyAtAcxiom());
        target.setExpectedQuantity(request.expectedQuantity());
        target.setFrequency(
                request.frequency() == null || request.frequency().isBlank()
                        ? DEFAULT_FREQUENCY
                        : request.frequency());
        target.setSpecialInstructions(request.specialInstructions());
        target.setEventId(request.eventId());
        target.setMediaIds(request.mediaIds());
        target.setMailMonth(request.mailMonth());
        target.setMediaOutQuantity(request.mediaOutQuantity());
        target.setChangesToMatchbackDb(request.changesToMatchbackDb());
        target.setSelectionCriteria(request.selectionCriteria());
        target.setMatchbackField(request.matchbackField());
        target.setChangeTo(request.changeTo());
        target.setDbmWorkOrderNumber(request.dbmWorkOrderNumber());
        target.setDbmCompletionNotes(request.dbmCompletionNotes());
        target.setTotalRecordsUpdated(request.totalRecordsUpdated());
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

    private DbmWorkOrderResponse toResponse(Case caseEntity, DbmWorkOrder dbmWorkOrder) {
        return new DbmWorkOrderResponse(
                caseEntity.getId(),
                caseEntity.getCaseNumber(),
                caseEntity.getCaseType(),
                caseEntity.getCaseOwner(),
                caseEntity.getRequestedDueDate(),
                caseEntity.getPriority(),
                caseEntity.getSubject(),
                caseEntity.getStatus(),
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
                dbmWorkOrder.getDbmWorkOrderNumber(),
                dbmWorkOrder.getDbmCompletionNotes(),
                dbmWorkOrder.getTotalRecordsUpdated(),
                dbmWorkOrder.getCreatedAt(),
                dbmWorkOrder.getUpdatedAt()
        );
    }
}
