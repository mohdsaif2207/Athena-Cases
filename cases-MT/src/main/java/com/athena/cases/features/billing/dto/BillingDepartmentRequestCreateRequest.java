package com.athena.cases.features.billing.dto;

import com.athena.cases.features.billing.enums.BillingExtractType;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.enums.BillingRequestType;
import com.athena.cases.features.billing.enums.PreNoteRequestType;
import com.athena.cases.features.billing.enums.PriorHardDeclines;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Create payload — LLD §15.1.
 *
 * <p>Billing enumerations use typed enums. {@code priority}/{@code status} remain String
 * because shared case-header enums do not exist yet (live on {@code cases}).
 */
public record BillingDepartmentRequestCreateRequest(

        BillingRequestType requestType,

        @Size(max = 64)
        String clientId,

        @Size(max = 64)
        String campaignId,

        @Size(max = 64)
        String assignedTo,

        @NotBlank
        @Pattern(regexp = BillingValidationPatterns.PRIORITY,
                message = "priority must be High, Medium, or Low")
        String priority,

        @NotBlank
        @Pattern(regexp = BillingValidationPatterns.STATUS,
                message = "status must be a defined case status value")
        String status,

        @Size(max = 500)
        String reasonForImportance,

        Boolean dailyIssueReport,

        Integer approxNumberOfCoverages,

        @Digits(integer = 16, fraction = 2)
        BigDecimal approxRevenueImpact,

        LocalDate requestedDueDate,

        Long parentCaseId,

        LocalDate effectiveDate,

        Long segmentId,

        Long productId,

        LocalDate anticipatedReleaseDate,

        @Size(max = 5000)
        String requestDescription,

        BillingExtractType billingExtractType,

        PreNoteRequestType preNoteRequestType,

        @Size(max = 255)
        String billingInstitution,

        LocalDate targetPostDate,

        @Size(max = 255)
        String billSet,

        @Size(max = 255)
        String billingCycle,

        PriorHardDeclines priorHardDeclines,

        @Size(max = 500)
        String hardDeclineCodes,

        BillingHoldType billingHoldType,

        List<BillingHoldLevelCode> holdLevelCodes,

        @Size(max = 1000)
        String holdReason,

        Long billingHoldByProductId
) {
}
