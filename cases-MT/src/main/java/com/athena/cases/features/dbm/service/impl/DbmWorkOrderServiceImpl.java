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
        Instant now = Instant.now();
        String actor = TEMP_CASE_OWNER;

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalStateException("Case not found: " + caseId));

        DbmWorkOrder dbmWorkOrder = dbmWorkOrderRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalStateException(
                        "DBM Work Order not found for caseId: " + caseId));

        caseEntity.setSubject(request.subject());
        caseEntity.setDescription(request.description());
        caseEntity.setStatus(request.status());
        caseEntity.setPriority(request.priority());
        caseEntity.setRequestedDueDate(request.requestedDueDate());
        caseEntity.setClientId(request.clientId());
        caseEntity.setPendingDbmApproval(isCustomTransferType(request.transferType()));
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

        Case savedCase = caseRepository.save(caseEntity);
        DbmWorkOrder savedDbm = dbmWorkOrderRepository.save(dbmWorkOrder);

        return toResponse(savedCase, savedDbm);
    }

    @Override
    @Transactional(readOnly = true)
    public DbmWorkOrderResponse getByCaseId(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalStateException("Case not found: " + caseId));

        DbmWorkOrder dbmWorkOrder = dbmWorkOrderRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalStateException(
                        "DBM Work Order not found for caseId: " + caseId));

        return toResponse(caseEntity, dbmWorkOrder);
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
