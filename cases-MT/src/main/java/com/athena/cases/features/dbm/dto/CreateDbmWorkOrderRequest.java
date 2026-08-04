package com.athena.cases.features.dbm.dto;

import java.time.LocalDate;
import java.util.List;

import com.athena.cases.features.dbm.DbmConstants;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Create payload for DBM Work Order Request.
 * Case Owner and Case Number are server-populated and must not be supplied as required inputs.
 */
public record CreateDbmWorkOrderRequest(

        // --- Request Information (shared header fields set on Case) ---
        @NotBlank @Size(max = 32) String vendor,
        @NotNull LocalDate requestedDueDate,
        @NotBlank @Size(max = 16) String priority,
        @NotBlank @Size(max = 200) String subject,
        @NotBlank @Size(max = 64) String status,
        @Size(max = 1000) String description,
        boolean coreProcessorConversion,

        // --- File Request ---
        @NotBlank @Size(max = 64) String transferType,
        List<@NotBlank @Size(max = 64) String> coverageLevels,
        @NotBlank @Size(max = 8) String returnFileExpected,
        @Size(max = 8) String pgpKeyAtAcxiom,
        List<@NotBlank @Size(max = 64) String> requestedAccountTypes,
        @PositiveOrZero Integer expectedQuantity,
        @Size(max = 32) String frequency,
        String specialInstructions,

        // --- Marketing Research Request ---
        @NotBlank @Size(max = 64) String clientId,
        List<@NotBlank @Size(max = 64) String> spokenKeys,
        @Size(max = 64) String eventId,
        String mediaIds,
        @Size(max = 32) String mailMonth,
        @PositiveOrZero Integer mediaOutQuantity,
        boolean changesToMatchbackDb,
        String selectionCriteria,
        @Size(max = 200) String matchbackField,
        @Size(max = 200) String changeTo,

        // --- For DBM Use Only (optional on create; typically used by DBM team) ---
        @Size(max = 64) String dbmWorkOrderNumber,
        String dbmCompletionNotes,
        @PositiveOrZero Integer totalRecordsUpdated
) {
    @AssertTrue(message = "Special Instructions is required when Transfer Type is Other (Requires Description)")
    public boolean isSpecialInstructionsPresentWhenOther() {
        if (transferType == null || transferType.isBlank()) {
            return true;
        }
        if (!DbmConstants.TRANSFER_TYPE_OTHER.equalsIgnoreCase(transferType.trim())) {
            return true;
        }
        return specialInstructions != null && !specialInstructions.isBlank();
    }
}
