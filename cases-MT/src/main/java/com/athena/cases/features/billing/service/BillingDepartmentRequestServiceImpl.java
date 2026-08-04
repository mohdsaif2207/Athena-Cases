package com.athena.cases.features.billing.service;

import com.athena.cases.casemanagement.CaseManagementService;
import com.athena.cases.casemanagement.CaseRef;
import com.athena.cases.casemanagement.CreateCaseCommand;
import com.athena.cases.casemanagement.UpdateCaseHeaderCommand;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.enums.CaseTypeCode;
import com.athena.cases.features.billing.BillingConstants;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestCreateRequest;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestResponse;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestUpdateRequest;
import com.athena.cases.features.billing.entity.BillingDepartmentRequest;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.exception.BillingConflictException;
import com.athena.cases.features.billing.exception.BillingResourceNotFoundException;
import com.athena.cases.features.billing.exception.BillingValidationException;
import com.athena.cases.features.billing.lookup.BillingLookupService;
import com.athena.cases.features.billing.mapper.BillingDepartmentRequestMapper;
import com.athena.cases.features.billing.repository.BillingDepartmentRequestRepository;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.lookup.LookupService;
import com.athena.cases.permission.PermissionService;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.workflow.WorkflowRef;
import com.athena.cases.workflow.WorkflowService;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Billing Department Request orchestration — LLD §17.
 *
 * <p>Injects shared port interfaces only. No Billing adapters or placeholder beans.
 *
 * <p><b>Shared dependency — CaseRef:</b> currently exposes {@code caseId}, {@code caseNumber},
 * {@code version} only. Response fields {@code caseType}, {@code caseOwner}, {@code assignedTo},
 * {@code priority}, {@code status}, {@code parentCaseId}, {@code parentCaseNumber} are left null
 * until Case Management enriches {@code CaseRef} (or equivalent).
 *
 * <p><b>Shared dependency — businessCaseId / caseNumber:</b> Case Management allocates the
 * display id ({@code BIL######}) via {@link BusinessCaseIdService} and returns it on
 * {@link CaseRef#caseNumber()}. Billing copies that same value into {@code business_case_id}
 * (no second allocation).
 */
@Service
public class BillingDepartmentRequestServiceImpl implements BillingDepartmentRequestService {

    private static final Logger log = LoggerFactory.getLogger(BillingDepartmentRequestServiceImpl.class);

    private final BillingDepartmentRequestRepository billingRepository;
    private final BillingDepartmentRequestMapper mapper;
    private final CaseManagementService caseManagementService;
    private final WorkflowService workflowService;
    private final BillingLookupService billingLookupService;
    private final LookupService lookupService;
    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;

    public BillingDepartmentRequestServiceImpl(
            BillingDepartmentRequestRepository billingRepository,
            BillingDepartmentRequestMapper mapper,
            CaseManagementService caseManagementService,
            WorkflowService workflowService,
            BillingLookupService billingLookupService,
            LookupService lookupService,
            CurrentUserService currentUserService,
            PermissionService permissionService
    ) {
        this.billingRepository = billingRepository;
        this.mapper = mapper;
        this.caseManagementService = caseManagementService;
        this.workflowService = workflowService;
        this.billingLookupService = billingLookupService;
        this.lookupService = lookupService;
        this.currentUserService = currentUserService;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional
    public BillingDepartmentRequestResponse create(BillingDepartmentRequestCreateRequest request) {
        String userId = currentUserService.requireUserId();
        permissionService.require(userId, PermissionCodes.CASES_CREATE);

        String caseOwner = currentUserService.requireDisplayName();
        String priority = blankToDefault(request.priority(), BillingConstants.DEFAULT_PRIORITY);
        String status = blankToDefault(request.status(), BillingConstants.DEFAULT_STATUS);

        validateReferential(request.campaignId(), request.clientId(), request.segmentId(),
                request.productId(), request.billingHoldByProductId(), request.parentCaseId());
        // TODO assignedTo billing-access check — LookupService has no assignees API yet

        CaseRef caseRef = caseManagementService.createCase(new CreateCaseCommand(
                CaseTypeCode.BILLING_DEPARTMENT_REQUEST.name(),
                caseOwner,
                priority,
                status,
                request.parentCaseId()
        ));

        if (request.assignedTo() != null && !request.assignedTo().isBlank()) {
            caseRef = caseManagementService.updateCaseHeader(
                    caseRef.caseId(),
                    new UpdateCaseHeaderCommand(
                            priority,
                            status,
                            request.assignedTo(),
                            request.parentCaseId(),
                            caseRef.version()
                    )
            );
        }

        if (billingRepository.existsByCaseId(caseRef.caseId())) {
            throw new BillingConflictException(
                    "Billing Department Request already exists for caseId=" + caseRef.caseId());
        }

        // Same display id as shared cases.case_number (allocated once by Case Management).
        String businessCaseId = requireBillingDisplayId(caseRef.caseNumber());

        Instant now = Instant.now();
        BillingDepartmentRequest entity = new BillingDepartmentRequest();
        entity.setCaseId(caseRef.caseId());
        entity.setBusinessCaseId(businessCaseId);
        mapper.applyCreate(entity, request);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setVersion(1);
        billingRepository.save(entity);

        // Workflow + notification are owned by CaseManagementService.createCase
        // (enqueueReceivingTeamWork) — do not start a second instance here.
        log.info("billing department request created - caseId={} businessCaseId={} userId={}",
                caseRef.caseId(), entity.getBusinessCaseId(), userId);

        return toResponse(caseRef, entity, safeWorkflow(caseRef.caseId()));
    }

    @Override
    public BillingDepartmentRequestResponse getByCaseId(Long caseId) {
        String userId = currentUserService.requireUserId();
        permissionService.require(userId, PermissionCodes.CASES_VIEW);

        CaseRef caseRef = caseManagementService.getCase(caseId);
        BillingDepartmentRequest entity = billingRepository.findByCaseId(caseId)
                .orElseThrow(() -> new BillingResourceNotFoundException(
                        "Billing Department Request not found for caseId=" + caseId));

        return toResponse(caseRef, entity, safeWorkflow(caseId));
    }

    @Override
    @Transactional
    public BillingDepartmentRequestResponse update(Long caseId, BillingDepartmentRequestUpdateRequest request) {
        String userId = currentUserService.requireUserId();
        permissionService.require(userId, PermissionCodes.CASES_EDIT);

        CaseRef caseRef = caseManagementService.getCase(caseId);
        BillingDepartmentRequest entity = billingRepository.findByCaseId(caseId)
                .orElseThrow(() -> new BillingResourceNotFoundException(
                        "Billing Department Request not found for caseId=" + caseId));

        if (caseRef.version() == null || request.version() == null
                || caseRef.version().intValue() != request.version()) {
            throw new BillingConflictException(
                    "Version conflict for caseId=" + caseId + "; reload and retry");
        }

        validateReferential(request.campaignId(), request.clientId(), request.segmentId(),
                request.productId(), request.billingHoldByProductId(), request.parentCaseId());
        // TODO assignedTo billing-access check — LookupService has no assignees API yet

        CaseRef updated = caseManagementService.updateCaseHeader(
                caseId,
                new UpdateCaseHeaderCommand(
                        request.priority(),
                        request.status(),
                        request.assignedTo(),
                        request.parentCaseId(),
                        caseRef.version()
                )
        );

        mapper.applyUpdate(entity, request);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(userId);
        billingRepository.save(entity);

        log.info("billing department request updated - caseId={} userId={}", caseId, userId);

        return toResponse(updated, entity, safeWorkflow(caseId));
    }

    @Override
    public List<BillingHoldLevelCode> listHoldLevels(BillingHoldType holdType) {
        return billingLookupService.listHoldLevels(holdType);
    }

    @Override
    public List<LookupItem> listAssignees() {
        return billingLookupService.listAssignees();
    }

    private BillingDepartmentRequestResponse toResponse(
            CaseRef caseRef,
            BillingDepartmentRequest entity,
            WorkflowRef workflow
    ) {
        return mapper.toResponse(
                caseRef,
                entity,
                workflow,
                resolveClientName(entity.getClientId()),
                resolveSegmentName(entity.getClientId(), entity.getSegmentId()),
                resolveProductName(entity.getProductId())
        );
    }

    /**
     * Billing extension CHECK requires {@code ^BIL[0-9]{6}$}; shared CM must have allocated that form.
     */
    private static String requireBillingDisplayId(String caseNumber) {
        if (caseNumber == null || !caseNumber.matches("^BIL[0-9]{6}$")) {
            throw new BillingValidationException(
                    "businessCaseId",
                    "shared case_number must be BIL###### for Billing Department Request; got: "
                            + caseNumber);
        }
        return caseNumber;
    }

    private void validateReferential(
            String campaignId,
            Long clientId,
            Long segmentId,
            Long productId,
            Long billingHoldByProductId,
            Long parentCaseId
    ) {
        if (campaignId != null && !campaignId.isBlank()) {
            boolean ok = billingLookupService.listCampaigns().stream()
                    .anyMatch(item -> campaignId.equals(item.code()) || campaignId.equals(item.id()));
            if (!ok) {
                throw new BillingValidationException("campaignId", "campaignId must be an active campaign");
            }
        }
        if (segmentId != null) {
            if (clientId == null) {
                throw new BillingValidationException("segmentId", "segmentId requires a selected clientId");
            }
            boolean ok = billingLookupService.listSegments(String.valueOf(clientId)).stream()
                    .anyMatch(item -> String.valueOf(segmentId).equals(item.id())
                            || String.valueOf(segmentId).equals(item.code()));
            if (!ok) {
                throw new BillingValidationException("segmentId",
                        "segmentId must be an active segment for the selected client");
            }
        }
        validateProduct(productId, "productId");
        validateProduct(billingHoldByProductId, "billingHoldByProductId");
        if (parentCaseId != null) {
            try {
                caseManagementService.getCase(parentCaseId);
            } catch (RuntimeException ex) {
                throw new BillingValidationException("parentCaseId",
                        "parentCaseId must reference an existing case");
            }
            if (!billingLookupService.isBillingCase(parentCaseId)) {
                throw new BillingValidationException("parentCaseId",
                        "parentCaseId must reference a Billing Department Request case");
            }
        }
    }

    private void validateProduct(Long productId, String field) {
        if (productId == null) {
            return;
        }
        boolean ok = billingLookupService.listProducts(null).stream()
                .anyMatch(item -> String.valueOf(productId).equals(item.id())
                        || String.valueOf(productId).equals(item.code()));
        if (!ok) {
            throw new BillingValidationException(field, field + " must be a valid active product");
        }
    }

    private String resolveClientName(Long clientId) {
        if (clientId == null) {
            return null;
        }
        String key = String.valueOf(clientId);
        return lookupService.searchClients(null).stream()
                .filter(item -> key.equals(item.id()) || key.equals(item.code()))
                .map(LookupItem::label)
                .findFirst()
                .orElse(null);
    }

    private String resolveSegmentName(Long clientId, Long segmentId) {
        if (clientId == null || segmentId == null) {
            return null;
        }
        String key = String.valueOf(segmentId);
        return billingLookupService.listSegments(String.valueOf(clientId)).stream()
                .filter(item -> key.equals(item.id()) || key.equals(item.code()))
                .map(LookupItem::label)
                .findFirst()
                .orElse(null);
    }

    private String resolveProductName(Long productId) {
        if (productId == null) {
            return null;
        }
        String key = String.valueOf(productId);
        return billingLookupService.listProducts(null).stream()
                .filter(item -> key.equals(item.id()) || key.equals(item.code()))
                .map(LookupItem::label)
                .findFirst()
                .orElse(null);
    }

    private WorkflowRef safeWorkflow(Long caseId) {
        try {
            return workflowService.getByCaseId(caseId);
        } catch (RuntimeException ex) {
            log.debug("workflow not found for caseId={}", caseId);
            return null;
        }
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
