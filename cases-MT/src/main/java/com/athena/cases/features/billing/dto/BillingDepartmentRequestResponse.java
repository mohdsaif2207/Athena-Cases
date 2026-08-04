package com.athena.cases.features.billing.dto;

import com.athena.cases.features.billing.enums.BillingExtractType;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.enums.BillingRequestType;
import com.athena.cases.features.billing.enums.PreNoteRequestType;
import com.athena.cases.features.billing.enums.PriorHardDeclines;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Create / View / Edit response — LLD §15.3.
 *
 * <p>{@code businessCaseId} added per approved Phase-1 decision #7.
 * {@code priority}/{@code status}/{@code caseType} remain String (case-header / shared codes).
 */
public record BillingDepartmentRequestResponse(
        Long caseId,
        String caseNumber,
        String businessCaseId,
        String caseType,
        String caseOwner,
        String assignedTo,
        String priority,
        String status,
        Long parentCaseId,
        String parentCaseNumber,
        BillingRequestType requestType,
        String clientId,
        String clientName,
        String campaignId,
        String reasonForImportance,
        boolean dailyIssueReport,
        Integer approxNumberOfCoverages,
        BigDecimal approxRevenueImpact,
        LocalDate requestedDueDate,
        LocalDate effectiveDate,
        Long segmentId,
        String segmentName,
        Long productId,
        String productName,
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
        Long billingHoldByProductId,
        Integer version,
        Instant createdAt,
        Instant updatedAt,
        Long workflowId,
        String workflowStatus
) {
}
