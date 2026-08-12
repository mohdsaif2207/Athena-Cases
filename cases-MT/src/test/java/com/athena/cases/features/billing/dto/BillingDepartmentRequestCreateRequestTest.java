package com.athena.cases.features.billing.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.athena.cases.features.billing.enums.BillingExtractType;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.enums.BillingRequestType;
import com.athena.cases.features.billing.enums.PreNoteRequestType;
import com.athena.cases.features.billing.enums.PriorHardDeclines;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class BillingDepartmentRequestCreateRequestTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Test
    void should_createRecordAndExposeAccessors() {
        // Arrange
        BillingDepartmentRequestCreateRequest request = validRequest();

        // Assert
        assertThat(request.requestType()).isEqualTo(BillingRequestType.EXTRACT);
        assertThat(request.clientId()).isEqualTo("CLIENT-01");
        assertThat(request.campaignId()).isEqualTo("CAMP-01");
        assertThat(request.assignedTo()).isEqualTo("charan");
        assertThat(request.priority()).isEqualTo("Medium");
        assertThat(request.status()).isEqualTo("Requested");
        assertThat(request.reasonForImportance()).isEqualTo("Important follow-up");
        assertThat(request.dailyIssueReport()).isTrue();
        assertThat(request.approxNumberOfCoverages()).isEqualTo(12);
        assertThat(request.approxRevenueImpact()).isEqualByComparingTo("1234.56");
        assertThat(request.requestedDueDate()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(request.parentCaseId()).isEqualTo(2001L);
        assertThat(request.effectiveDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(request.segmentId()).isEqualTo(42L);
        assertThat(request.productId()).isEqualTo(84L);
        assertThat(request.anticipatedReleaseDate()).isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(request.requestDescription()).isEqualTo("Investigate the billing request");
        assertThat(request.billingExtractType()).isEqualTo(BillingExtractType.BILLING);
        assertThat(request.preNoteRequestType()).isEqualTo(PreNoteRequestType.ALL);
        assertThat(request.billingInstitution()).isEqualTo("Franklin Madison");
        assertThat(request.targetPostDate()).isEqualTo(LocalDate.of(2026, 9, 20));
        assertThat(request.billSet()).isEqualTo("Bill Set A");
        assertThat(request.billingCycle()).isEqualTo("Monthly");
        assertThat(request.priorHardDeclines()).isEqualTo(PriorHardDeclines.YES);
        assertThat(request.hardDeclineCodes()).isEqualTo("HD1,HD2");
        assertThat(request.billingHoldType()).isEqualTo(BillingHoldType.CLIENT_LEVEL);
        assertThat(request.holdLevelCodes()).containsExactly(
                BillingHoldLevelCode.BILLING,
                BillingHoldLevelCode.REFUND);
        assertThat(request.holdReason()).isEqualTo("Hold for manual review");
        assertThat(request.billingHoldByProductId()).isEqualTo(777L);
    }

    @Test
    void should_supportEqualsHashCodeAndToString() {
        // Arrange
        BillingDepartmentRequestCreateRequest left = validRequest();
        BillingDepartmentRequestCreateRequest right = validRequest();
        BillingDepartmentRequestCreateRequest different = new BillingDepartmentRequestCreateRequest(
                BillingRequestType.RESEARCH,
                "CLIENT-01",
                "CAMP-01",
                "charan",
                "High",
                "Requested",
                "Important follow-up",
                true,
                12,
                new BigDecimal("1234.56"),
                LocalDate.of(2026, 8, 31),
                2001L,
                LocalDate.of(2026, 9, 1),
                42L,
                84L,
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
                777L
        );

        // Assert
        assertThat(left).isEqualTo(right);
        assertThat(left).hasSameHashCodeAs(right);
        assertThat(left).isNotEqualTo(different);
        assertThat(left.toString()).contains("requestType=EXTRACT", "priority=Medium", "status=Requested");
    }

    @Test
    void should_passValidationForBoundaryValues() {
        // Arrange
        BillingDepartmentRequestCreateRequest request = new BillingDepartmentRequestCreateRequest(
                BillingRequestType.SCHEDULE,
                repeated('C', 64),
                repeated('M', 64),
                repeated('A', 64),
                "Low",
                "Requested",
                repeated('R', 500),
                Boolean.FALSE,
                123,
                new BigDecimal("9999999999999999.99"),
                LocalDate.of(2026, 12, 31),
                3001L,
                LocalDate.of(2026, 11, 30),
                42L,
                84L,
                LocalDate.of(2026, 10, 1),
                repeated('D', 5000),
                BillingExtractType.REBILL,
                PreNoteRequestType.CHANGES_ONLY,
                repeated('I', 255),
                LocalDate.of(2026, 10, 15),
                repeated('B', 255),
                repeated('Y', 255),
                PriorHardDeclines.NO,
                repeated('H', 500),
                BillingHoldType.SEGMENT_LEVEL,
                List.of(BillingHoldLevelCode.ALL, BillingHoldLevelCode.REFUND),
                repeated('L', 1000),
                888L
        );

        // Assert
        assertThat(violationNames(request)).isEmpty();
    }

    @Test
    void should_rejectBlankRequiredFields() {
        // Arrange
        BillingDepartmentRequestCreateRequest request = new BillingDepartmentRequestCreateRequest(
                BillingRequestType.EXTRACT,
                null,
                null,
                null,
                null,
                "",
                " ",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Assert
        assertThat(violationNames(request)).containsExactlyInAnyOrder("priority", "status");
    }

    @Test
    void should_rejectValuesExceedingSizeAndPrecisionLimits() {
        // Arrange
        BillingDepartmentRequestCreateRequest request = new BillingDepartmentRequestCreateRequest(
                BillingRequestType.EXTRACT,
                repeated('C', 65),
                repeated('M', 65),
                repeated('A', 65),
                "Medium",
                "Requested",
                repeated('R', 501),
                Boolean.TRUE,
                12,
                new BigDecimal("12345678901234567.123"),
                LocalDate.of(2026, 12, 31),
                3001L,
                LocalDate.of(2026, 11, 30),
                42L,
                84L,
                LocalDate.of(2026, 10, 1),
                repeated('D', 5001),
                BillingExtractType.BILLING,
                PreNoteRequestType.ALL,
                repeated('I', 256),
                LocalDate.of(2026, 10, 15),
                repeated('B', 256),
                repeated('Y', 256),
                PriorHardDeclines.NO,
                repeated('H', 501),
                BillingHoldType.CLIENT_LEVEL,
                List.of(BillingHoldLevelCode.BILLING),
                repeated('L', 1001),
                888L
        );

        // Assert
        Set<String> violations = violationNames(request);
        assertThat(violations).contains(
                "clientId",
                "campaignId",
                "assignedTo",
                "reasonForImportance",
                "approxRevenueImpact",
                "requestDescription",
                "billingInstitution",
                "billSet",
                "billingCycle",
                "hardDeclineCodes",
                "holdReason"
        );
    }

    private static BillingDepartmentRequestCreateRequest validRequest() {
        return new BillingDepartmentRequestCreateRequest(
                BillingRequestType.EXTRACT,
                "CLIENT-01",
                "CAMP-01",
                "charan",
                "Medium",
                "Requested",
                "Important follow-up",
                Boolean.TRUE,
                12,
                new BigDecimal("1234.56"),
                LocalDate.of(2026, 8, 31),
                2001L,
                LocalDate.of(2026, 9, 1),
                42L,
                84L,
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
                777L
        );
    }

    private static Set<String> violationNames(BillingDepartmentRequestCreateRequest request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private static String repeated(char value, int length) {
        return String.valueOf(value).repeat(length);
    }
}
