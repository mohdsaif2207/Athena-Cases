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

class BillingDepartmentRequestUpdateRequestTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Test
    void should_createRecordAndExposeAccessors() {
        // Arrange
        BillingDepartmentRequestUpdateRequest request = validRequest();

        // Assert
        assertThat(request.version()).isEqualTo(4);
        assertThat(request.priority()).isEqualTo("High");
        assertThat(request.status()).isEqualTo("In Progress");
        assertThat(request.requestDescription()).isEqualTo("Investigate the billing request");
    }

    @Test
    void should_supportEqualsHashCodeAndToString() {
        // Arrange
        BillingDepartmentRequestUpdateRequest left = validRequest();
        BillingDepartmentRequestUpdateRequest right = validRequest();
        BillingDepartmentRequestUpdateRequest different = new BillingDepartmentRequestUpdateRequest(
                BillingRequestType.RESEARCH,
                "CLIENT-01",
                "CAMP-01",
                "charan",
                "High",
                "In Progress",
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
                777L,
                5
        );

        // Assert
        assertThat(left).isEqualTo(right);
        assertThat(left).hasSameHashCodeAs(right);
        assertThat(left).isNotEqualTo(different);
        assertThat(left.toString()).contains("version=4", "priority=High", "status=In Progress");
    }

    @Test
    void should_passValidationForBoundaryValues() {
        // Arrange
        BillingDepartmentRequestUpdateRequest request = new BillingDepartmentRequestUpdateRequest(
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
                888L,
                1
        );

        // Assert
        assertThat(violationNames(request)).isEmpty();
    }

    @Test
    void should_rejectBlankRequiredFieldsAndNullVersion() {
        // Arrange
        BillingDepartmentRequestUpdateRequest request = new BillingDepartmentRequestUpdateRequest(
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
                null,
                null
        );

        // Assert
        assertThat(violationNames(request)).containsExactlyInAnyOrder("priority", "status", "version");
    }

    @Test
    void should_rejectValuesExceedingSizeAndPrecisionLimits() {
        // Arrange
        BillingDepartmentRequestUpdateRequest request = new BillingDepartmentRequestUpdateRequest(
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
                888L,
                1
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

    private static BillingDepartmentRequestUpdateRequest validRequest() {
        return new BillingDepartmentRequestUpdateRequest(
                BillingRequestType.EXTRACT,
                "CLIENT-01",
                "CAMP-01",
                "charan",
                "High",
                "In Progress",
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
                777L,
                4
        );
    }

    private static Set<String> violationNames(BillingDepartmentRequestUpdateRequest request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private static String repeated(char value, int length) {
        return String.valueOf(value).repeat(length);
    }
}
