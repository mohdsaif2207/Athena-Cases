package com.athena.cases.features.dbm.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Full DBM Work Order Request view including shared Case header fields.
 */
public record DbmWorkOrderResponse(

        // --- Shared Case identity / header ---
        Long caseId,
        String caseNumber,
        String caseType,
        String caseOwner,
        LocalDate requestedDueDate,
        String priority,
        String subject,
        String status,
        String description,
        String clientId,
        boolean pendingDbmApproval,

        // --- Request Information (DBM) ---
        String vendor,
        boolean coreProcessorConversion,

        // --- File Request ---
        String transferType,
        List<String> coverageLevels,
        String returnFileExpected,
        String pgpKeyAtAcxiom,
        List<String> requestedAccountTypes,
        Integer expectedQuantity,
        String frequency,
        String specialInstructions,

        // --- Marketing Research Request ---
        List<String> spokenKeys,
        String eventId,
        String mediaIds,
        String mailMonth,
        Integer mediaOutQuantity,
        boolean changesToMatchbackDb,
        String selectionCriteria,
        String matchbackField,
        String changeTo,

        // --- For DBM Use Only ---
        String dbmWorkOrderNumber,
        String dbmCompletionNotes,
        Integer totalRecordsUpdated,

        Instant createdAt,
        Instant updatedAt
) {
}
