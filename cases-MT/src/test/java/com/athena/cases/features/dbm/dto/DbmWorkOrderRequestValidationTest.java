package com.athena.cases.features.dbm.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.athena.cases.features.dbm.DbmConstants;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Bean Validation tests for DBM create/update payloads (US Scenarios 3, 4, 9).
 */
class DbmWorkOrderRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void should_pass_when_createHasAllMandatoryFields() {
        Set<ConstraintViolation<CreateDbmWorkOrderRequest>> violations =
                validator.validate(validCreate("Account Update File", null));

        assertThat(violations).isEmpty();
    }

    @Test
    void should_fail_when_vendorBlank() {
        CreateDbmWorkOrderRequest request = new CreateDbmWorkOrderRequest(
                " ",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("vendor"));
    }

    @Test
    void should_fail_when_subjectMissing() {
        CreateDbmWorkOrderRequest request = new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                null,
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
    }

    @Test
    void should_fail_when_requestedDueDateNull() {
        CreateDbmWorkOrderRequest request = new CreateDbmWorkOrderRequest(
                "Acxiom",
                null,
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("requestedDueDate"));
    }

    @Test
    void should_fail_when_clientIdBlank() {
        CreateDbmWorkOrderRequest request = new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                " ",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("clientId"));
    }

    @Test
    void should_requireSpecialInstructions_when_transferTypeOther() {
        Set<ConstraintViolation<CreateDbmWorkOrderRequest>> violations =
                validator.validate(validCreate(DbmConstants.TRANSFER_TYPE_OTHER, "  "));

        assertThat(violations)
                .anyMatch(v -> v.getMessage().contains("Special Instructions"));
    }

    @Test
    void should_pass_when_transferTypeOtherWithSpecialInstructions() {
        Set<ConstraintViolation<CreateDbmWorkOrderRequest>> violations =
                validator.validate(validCreate(DbmConstants.TRANSFER_TYPE_OTHER, "Need custom file layout"));

        assertThat(violations).isEmpty();
    }

    @Test
    void should_notRequireSpecialInstructions_when_transferTypeNotOther() {
        Set<ConstraintViolation<CreateDbmWorkOrderRequest>> violations =
                validator.validate(validCreate("Cancel File", null));

        assertThat(violations).isEmpty();
    }

    @Test
    void should_fail_when_expectedQuantityNegative() {
        CreateDbmWorkOrderRequest request = new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                -5,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("expectedQuantity"));
    }

    @Test
    void should_fail_when_totalRecordsUpdatedNegative() {
        CreateDbmWorkOrderRequest request = new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                -1);

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("totalRecordsUpdated"));
    }

    @Test
    void should_fail_when_subjectExceedsMaxLength() {
        String tooLong = "x".repeat(201);
        CreateDbmWorkOrderRequest request = new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                tooLong,
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
    }

    @Test
    void should_requireSpecialInstructions_onUpdate_when_transferTypeOther() {
        UpdateDbmWorkOrderRequest request = new UpdateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                DbmConstants.TRANSFER_TYPE_OTHER,
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getMessage().contains("Special Instructions"));
    }

    @Test
    void should_allowNullOptionalNumerics() {
        Set<ConstraintViolation<CreateDbmWorkOrderRequest>> violations =
                validator.validate(validCreate("Account Update File", null));

        assertThat(violations).isEmpty();
    }

    private static CreateDbmWorkOrderRequest validCreate(String transferType, String specialInstructions) {
        return new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                transferType,
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                specialInstructions,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
