package com.athena.cases.features.billing.mapper;

import com.athena.cases.casemanagement.CaseRef;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestCreateRequest;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestResponse;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestUpdateRequest;
import com.athena.cases.features.billing.entity.BillingDepartmentRequest;
import com.athena.cases.features.billing.entity.BillingRequestHoldLevel;
import com.athena.cases.features.billing.enums.BillingExtractType;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.enums.BillingRequestType;
import com.athena.cases.features.billing.enums.PreNoteRequestType;
import com.athena.cases.features.billing.enums.PriorHardDeclines;
import com.athena.cases.workflow.WorkflowRef;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Manual entity ↔ DTO mapping (MapStruct not adopted).
 *
 * <p>Response mapping uses only {@link CaseRef} fields from Case Management
 * ({@code caseId}, {@code caseNumber}, {@code version}) plus Billing entity /
 * workflow / lookup data. Header fields not on {@code CaseRef} are left null.
 */
@Component
public class BillingDepartmentRequestMapper {

    public void applyCreate(BillingDepartmentRequest entity, BillingDepartmentRequestCreateRequest request) {
        applyBillingFields(
                entity,
                request.requestType(),
                request.clientId(),
                request.campaignId(),
                request.reasonForImportance(),
                request.dailyIssueReport(),
                request.approxNumberOfCoverages(),
                request.approxRevenueImpact(),
                request.requestedDueDate(),
                request.effectiveDate(),
                request.segmentId(),
                request.productId(),
                request.anticipatedReleaseDate(),
                request.requestDescription(),
                request.billingExtractType(),
                request.preNoteRequestType(),
                request.billingInstitution(),
                request.targetPostDate(),
                request.billSet(),
                request.billingCycle(),
                request.priorHardDeclines(),
                request.hardDeclineCodes(),
                request.billingHoldType(),
                request.holdLevelCodes(),
                request.holdReason(),
                request.billingHoldByProductId()
        );
    }

    public void applyUpdate(BillingDepartmentRequest entity, BillingDepartmentRequestUpdateRequest request) {
        applyBillingFields(
                entity,
                request.requestType(),
                request.clientId(),
                request.campaignId(),
                request.reasonForImportance(),
                request.dailyIssueReport(),
                request.approxNumberOfCoverages(),
                request.approxRevenueImpact(),
                request.requestedDueDate(),
                request.effectiveDate(),
                request.segmentId(),
                request.productId(),
                request.anticipatedReleaseDate(),
                request.requestDescription(),
                request.billingExtractType(),
                request.preNoteRequestType(),
                request.billingInstitution(),
                request.targetPostDate(),
                request.billSet(),
                request.billingCycle(),
                request.priorHardDeclines(),
                request.hardDeclineCodes(),
                request.billingHoldType(),
                request.holdLevelCodes(),
                request.holdReason(),
                request.billingHoldByProductId()
        );
    }

    public BillingDepartmentRequestResponse toResponse(
            CaseRef caseRef,
            BillingDepartmentRequest entity,
            WorkflowRef workflow,
            String clientName,
            String segmentName,
            String productName
    ) {
        List<BillingHoldLevelCode> codes = entity.getHoldLevels().stream()
                .map(BillingRequestHoldLevel::getHoldLevelCode)
                .toList();

        Integer version = caseRef.version() == null ? null : caseRef.version().intValue();

        return new BillingDepartmentRequestResponse(
                caseRef.caseId(),
                caseRef.caseNumber(),
                entity.getBusinessCaseId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                entity.getRequestType(),
                entity.getClientId(),
                clientName,
                entity.getCampaignId(),
                entity.getReasonForImportance(),
                entity.isDailyIssueReport(),
                entity.getApproxNumberOfCoverages(),
                entity.getApproxRevenueImpact(),
                entity.getRequestedDueDate(),
                entity.getEffectiveDate(),
                entity.getSegmentId(),
                segmentName,
                entity.getProductId(),
                productName,
                entity.getAnticipatedReleaseDate(),
                entity.getRequestDescription(),
                entity.getBillingExtractType(),
                entity.getPreNoteRequestType(),
                entity.getBillingInstitution(),
                entity.getTargetPostDate(),
                entity.getBillSet(),
                entity.getBillingCycle(),
                entity.getPriorHardDeclines(),
                entity.getHardDeclineCodes(),
                entity.getBillingHoldType(),
                codes,
                entity.getHoldReason(),
                entity.getBillingHoldByProductId(),
                version,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                workflow == null ? null : workflow.workflowInstanceId(),
                workflow == null ? null : workflow.statusCode()
        );
    }

    private void applyBillingFields(
            BillingDepartmentRequest entity,
            BillingRequestType requestType,
            Long clientId,
            String campaignId,
            String reasonForImportance,
            Boolean dailyIssueReport,
            Integer approxNumberOfCoverages,
            BigDecimal approxRevenueImpact,
            LocalDate requestedDueDate,
            LocalDate effectiveDate,
            Long segmentId,
            Long productId,
            LocalDate anticipatedReleaseDate,
            String requestDescription,
            BillingExtractType billingExtractType,
            PreNoteRequestType preNoteRequestType,
            String billingInstitution,
            LocalDate targetPostDate,
            String billSet,
            String billingCycle,
            PriorHardDeclines priorHardDeclines,
            String hardDeclineCodes,
            BillingHoldType billingHoldType,
            List<BillingHoldLevelCode> holdLevelCodes,
            String holdReason,
            Long billingHoldByProductId
    ) {
        entity.setRequestType(requestType);
        entity.setClientId(clientId);
        entity.setCampaignId(campaignId);
        entity.setReasonForImportance(reasonForImportance);
        entity.setDailyIssueReport(Boolean.TRUE.equals(dailyIssueReport));
        entity.setApproxNumberOfCoverages(approxNumberOfCoverages);
        entity.setApproxRevenueImpact(approxRevenueImpact);
        entity.setRequestedDueDate(requestedDueDate);
        entity.setEffectiveDate(effectiveDate);
        entity.setSegmentId(segmentId);
        entity.setProductId(productId);
        entity.setAnticipatedReleaseDate(anticipatedReleaseDate);
        entity.setRequestDescription(requestDescription);
        entity.setBillingExtractType(billingExtractType);
        entity.setPreNoteRequestType(preNoteRequestType);
        entity.setBillingInstitution(billingInstitution);
        entity.setTargetPostDate(targetPostDate);
        entity.setBillSet(billSet);
        entity.setBillingCycle(billingCycle);
        entity.setPriorHardDeclines(priorHardDeclines);
        entity.setHardDeclineCodes(hardDeclineCodes);
        entity.setBillingHoldType(billingHoldType);
        entity.setHoldReason(holdReason);
        entity.setBillingHoldByProductId(billingHoldByProductId);
        replaceHoldLevels(entity, holdLevelCodes);
    }

    private void replaceHoldLevels(BillingDepartmentRequest entity, List<BillingHoldLevelCode> codes) {
        entity.clearHoldLevels();
        if (codes == null || codes.isEmpty()) {
            return;
        }
        Set<BillingHoldLevelCode> unique = new LinkedHashSet<>(codes);
        Instant now = Instant.now();
        for (BillingHoldLevelCode code : unique) {
            BillingRequestHoldLevel row = new BillingRequestHoldLevel();
            row.setHoldLevelCode(code);
            row.setCreatedAt(now);
            entity.addHoldLevel(row);
        }
    }
}
