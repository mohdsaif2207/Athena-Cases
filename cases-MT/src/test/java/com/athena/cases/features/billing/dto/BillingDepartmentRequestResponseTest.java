package com.athena.cases.features.billing.dto;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Test;

class BillingDepartmentRequestResponseTest {

    @Test
    void should_createRecordAndExposeAccessors() {
        // Arrange
        BillingDepartmentRequestResponse response = sampleResponse();

        // Assert
        assertThat(response.caseId()).isEqualTo(1001L);
        assertThat(response.caseNumber()).isEqualTo("CASE-1001");
        assertThat(response.businessCaseId()).isEqualTo("BIL1001");
        assertThat(response.caseType()).isEqualTo("Billing Department Request");
        assertThat(response.caseOwner()).isEqualTo("charan");
        assertThat(response.assignedTo()).isEqualTo("MTcharan");
        assertThat(response.priority()).isEqualTo("Medium");
        assertThat(response.status()).isEqualTo("Requested");
        assertThat(response.parentCaseId()).isEqualTo(2001L);
        assertThat(response.parentCaseNumber()).isEqualTo("CASE-2001");
        assertThat(response.requestType()).isEqualTo(BillingRequestType.EXTRACT);
        assertThat(response.clientId()).isEqualTo("CLIENT-01");
        assertThat(response.clientName()).isEqualTo("ABC Bank");
        assertThat(response.campaignId()).isEqualTo("CAMP-01");
        assertThat(response.reasonForImportance()).isEqualTo("Important follow-up");
        assertThat(response.dailyIssueReport()).isTrue();
        assertThat(response.approxNumberOfCoverages()).isEqualTo(12);
        assertThat(response.approxRevenueImpact()).isEqualByComparingTo("1234.56");
        assertThat(response.requestedDueDate()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(response.effectiveDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(response.segmentId()).isEqualTo(42L);
        assertThat(response.segmentName()).isEqualTo("Segment A");
        assertThat(response.productId()).isEqualTo(84L);
        assertThat(response.productName()).isEqualTo("Product A");
        assertThat(response.anticipatedReleaseDate()).isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(response.requestDescription()).isEqualTo("Investigate the billing request");
        assertThat(response.billingExtractType()).isEqualTo(BillingExtractType.BILLING);
        assertThat(response.preNoteRequestType()).isEqualTo(PreNoteRequestType.ALL);
        assertThat(response.billingInstitution()).isEqualTo("Franklin Madison");
        assertThat(response.targetPostDate()).isEqualTo(LocalDate.of(2026, 9, 20));
        assertThat(response.billSet()).isEqualTo("Bill Set A");
        assertThat(response.billingCycle()).isEqualTo("Monthly");
        assertThat(response.priorHardDeclines()).isEqualTo(PriorHardDeclines.YES);
        assertThat(response.hardDeclineCodes()).isEqualTo("HD1,HD2");
        assertThat(response.billingHoldType()).isEqualTo(BillingHoldType.CLIENT_LEVEL);
        assertThat(response.holdLevelCodes()).containsExactly(
                BillingHoldLevelCode.BILLING,
                BillingHoldLevelCode.REFUND);
        assertThat(response.holdReason()).isEqualTo("Hold for manual review");
        assertThat(response.billingHoldByProductId()).isEqualTo(777L);
        assertThat(response.version()).isEqualTo(4);
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-08-04T10:15:30Z"));
        assertThat(response.updatedAt()).isEqualTo(Instant.parse("2026-08-04T10:20:30Z"));
        assertThat(response.workflowId()).isEqualTo(9001L);
        assertThat(response.workflowStatus()).isEqualTo("Pending Assignment");
    }

    @Test
    void should_supportEqualsHashCodeAndToString() {
        // Arrange
        BillingDepartmentRequestResponse left = sampleResponse();
        BillingDepartmentRequestResponse right = sampleResponse();
        BillingDepartmentRequestResponse different = new BillingDepartmentRequestResponse(
                1001L,
                "CASE-1001",
                "BIL1001",
                "Billing Department Request",
                "charan",
                "MTcharan",
                "High",
                "Requested",
                2001L,
                "CASE-2001",
                BillingRequestType.EXTRACT,
                "CLIENT-01",
                "ABC Bank",
                "CAMP-01",
                "Important follow-up",
                true,
                12,
                new BigDecimal("1234.56"),
                LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 9, 1),
                42L,
                "Segment A",
                84L,
                "Product A",
                LocalDate.of(2026, 9, 15),
                "Investigate the billing request",
                BillingExtractType.BILLING,
                PreNoteRequestType.ALL,
                "Franklin Madison",
                LocalDate.of(2026, 9, 20),
                "Bill Set A",
                "Monthly",
                PriorHardDeclines.YES,
                "HD1,HD2",
                BillingHoldType.CLIENT_LEVEL,
                List.of(BillingHoldLevelCode.BILLING),
                "Hold for manual review",
                777L,
                4,
                Instant.parse("2026-08-04T10:15:30Z"),
                Instant.parse("2026-08-04T10:20:30Z"),
                9001L,
                "Pending Assignment"
        );

        // Assert
        assertThat(left).isEqualTo(right);
        assertThat(left).hasSameHashCodeAs(right);
        assertThat(left).isNotEqualTo(different);
        assertThat(left.toString()).contains("caseId=1001", "caseNumber=CASE-1001", "workflowStatus=Pending Assignment");
    }

    private static BillingDepartmentRequestResponse sampleResponse() {
        return new BillingDepartmentRequestResponse(
                1001L,
                "CASE-1001",
                "BIL1001",
                "Billing Department Request",
                "charan",
                "MTcharan",
                "Medium",
                "Requested",
                2001L,
                "CASE-2001",
                BillingRequestType.EXTRACT,
                "CLIENT-01",
                "ABC Bank",
                "CAMP-01",
                "Important follow-up",
                true,
                12,
                new BigDecimal("1234.56"),
                LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 9, 1),
                42L,
                "Segment A",
                84L,
                "Product A",
                LocalDate.of(2026, 9, 15),
                "Investigate the billing request",
                BillingExtractType.BILLING,
                PreNoteRequestType.ALL,
                "Franklin Madison",
                LocalDate.of(2026, 9, 20),
                "Bill Set A",
                "Monthly",
                PriorHardDeclines.YES,
                "HD1,HD2",
                BillingHoldType.CLIENT_LEVEL,
                List.of(BillingHoldLevelCode.BILLING, BillingHoldLevelCode.REFUND),
                "Hold for manual review",
                777L,
                4,
                Instant.parse("2026-08-04T10:15:30Z"),
                Instant.parse("2026-08-04T10:20:30Z"),
                9001L,
                "Pending Assignment"
        );
    }
}
