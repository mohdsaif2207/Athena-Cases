package com.athena.cases.features.exrt.dto;

public record ExrtCaseCreateResponse(
        Long caseId,
        String caseNumber,
        String caseType,
        String status,
        String caseOwner,
        String message
) {
}
