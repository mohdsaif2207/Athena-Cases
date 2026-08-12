package com.athena.cases.features.billing.entity;

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
import org.junit.jupiter.api.Test;

class BillingDepartmentRequestTest {

    @Test
    void should_startWithEmptyHoldLevelsAndDefaultFalseDailyIssueReport() {
        // Arrange
        BillingDepartmentRequest request = new BillingDepartmentRequest();

        // Act / Assert
        assertThat(request.getHoldLevels()).isNotNull().isEmpty();
        assertThat(request.isDailyIssueReport()).isFalse();
    }

    @Test
    void should_storeAndReturnAllFieldValues() {
        // Arrange
        BillingDepartmentRequest request = new BillingDepartmentRequest();
        Instant createdAt = Instant.parse("2026-08-04T10:15:30Z");
        Instant updatedAt = Instant.parse("2026-08-04T10:20:30Z");
        LocalDate requestedDueDate = LocalDate.of(2026, 8, 31);
        LocalDate effectiveDate = LocalDate.of(2026, 9, 1);
        LocalDate anticipatedReleaseDate = LocalDate.of(2026, 9, 15);
        LocalDate targetPostDate = LocalDate.of(2026, 9, 20);

        // Act
        request.setId(99L);
        request.setCaseId(1001L);
        request.setBusinessCaseId("BIL1001");
        request.setRequestType(BillingRequestType.EXTRACT);
        request.setClientId("CLIENT-01");
        request.setCampaignId("CAMP-01");
        request.setReasonForImportance("Important billing follow-up");
        request.setDailyIssueReport(true);
        request.setApproxNumberOfCoverages(12);
        request.setApproxRevenueImpact(new BigDecimal("1234.56"));
        request.setRequestedDueDate(requestedDueDate);
        request.setEffectiveDate(effectiveDate);
        request.setSegmentId(42L);
        request.setProductId(84L);
        request.setAnticipatedReleaseDate(anticipatedReleaseDate);
        request.setRequestDescription("Investigate the billing request");
        request.setBillingExtractType(BillingExtractType.BILLING);
        request.setPreNoteRequestType(PreNoteRequestType.ALL);
        request.setBillingInstitution("Franklin Madison");
        request.setTargetPostDate(targetPostDate);
        request.setBillSet("Bill Set A");
        request.setBillingCycle("Monthly");
        request.setPriorHardDeclines(PriorHardDeclines.YES);
        request.setHardDeclineCodes("HD1,HD2");
        request.setBillingHoldType(BillingHoldType.CLIENT_LEVEL);
        request.setHoldReason("Hold for manual review");
        request.setBillingHoldByProductId(777L);
        request.setCreatedAt(createdAt);
        request.setUpdatedAt(updatedAt);
        request.setCreatedBy("charan");
        request.setUpdatedBy("charan");
        request.setVersion(3);

        // Assert
        assertThat(request.getId()).isEqualTo(99L);
        assertThat(request.getCaseId()).isEqualTo(1001L);
        assertThat(request.getBusinessCaseId()).isEqualTo("BIL1001");
        assertThat(request.getRequestType()).isEqualTo(BillingRequestType.EXTRACT);
        assertThat(request.getClientId()).isEqualTo("CLIENT-01");
        assertThat(request.getCampaignId()).isEqualTo("CAMP-01");
        assertThat(request.getReasonForImportance()).isEqualTo("Important billing follow-up");
        assertThat(request.isDailyIssueReport()).isTrue();
        assertThat(request.getApproxNumberOfCoverages()).isEqualTo(12);
        assertThat(request.getApproxRevenueImpact()).isEqualByComparingTo("1234.56");
        assertThat(request.getRequestedDueDate()).isEqualTo(requestedDueDate);
        assertThat(request.getEffectiveDate()).isEqualTo(effectiveDate);
        assertThat(request.getSegmentId()).isEqualTo(42L);
        assertThat(request.getProductId()).isEqualTo(84L);
        assertThat(request.getAnticipatedReleaseDate()).isEqualTo(anticipatedReleaseDate);
        assertThat(request.getRequestDescription()).isEqualTo("Investigate the billing request");
        assertThat(request.getBillingExtractType()).isEqualTo(BillingExtractType.BILLING);
        assertThat(request.getPreNoteRequestType()).isEqualTo(PreNoteRequestType.ALL);
        assertThat(request.getBillingInstitution()).isEqualTo("Franklin Madison");
        assertThat(request.getTargetPostDate()).isEqualTo(targetPostDate);
        assertThat(request.getBillSet()).isEqualTo("Bill Set A");
        assertThat(request.getBillingCycle()).isEqualTo("Monthly");
        assertThat(request.getPriorHardDeclines()).isEqualTo(PriorHardDeclines.YES);
        assertThat(request.getHardDeclineCodes()).isEqualTo("HD1,HD2");
        assertThat(request.getBillingHoldType()).isEqualTo(BillingHoldType.CLIENT_LEVEL);
        assertThat(request.getHoldReason()).isEqualTo("Hold for manual review");
        assertThat(request.getBillingHoldByProductId()).isEqualTo(777L);
        assertThat(request.getCreatedAt()).isEqualTo(createdAt);
        assertThat(request.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(request.getCreatedBy()).isEqualTo("charan");
        assertThat(request.getUpdatedBy()).isEqualTo("charan");
        assertThat(request.getVersion()).isEqualTo(3);
    }

    @Test
    void should_maintainHoldLevelAssociation_whenAddingAndClearing() {
        // Arrange
        BillingDepartmentRequest request = new BillingDepartmentRequest();
        BillingRequestHoldLevel holdLevel = new BillingRequestHoldLevel();
        holdLevel.setHoldLevelCode(BillingHoldLevelCode.BILLING);

        // Act
        request.addHoldLevel(holdLevel);

        // Assert
        assertThat(request.getHoldLevels()).containsExactly(holdLevel);
        assertThat(holdLevel.getBillingRequest()).isSameAs(request);

        // Act
        request.clearHoldLevels();

        // Assert
        assertThat(request.getHoldLevels()).isEmpty();
        assertThat(holdLevel.getBillingRequest()).isNull();
    }

    @Test
    void should_useIdForEqualityAndHashCode() {
        // Arrange
        BillingDepartmentRequest left = new BillingDepartmentRequest();
        BillingDepartmentRequest right = new BillingDepartmentRequest();
        BillingDepartmentRequest different = new BillingDepartmentRequest();

        left.setId(11L);
        right.setId(11L);
        different.setId(12L);

        // Assert
        assertThat(left).isEqualTo(right);
        assertThat(left).hasSameHashCodeAs(right);
        assertThat(left).isNotEqualTo(different);
        assertThat(left).isNotEqualTo(new Object());
    }
}
