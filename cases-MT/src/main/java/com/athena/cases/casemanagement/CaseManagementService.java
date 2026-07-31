package com.athena.cases.casemanagement;

/**
 * Port for shared case header lifecycle.
 * Feature modules depend on this interface only — never on peer features.
 */
public interface CaseManagementService {

    CaseRef createCase(CreateCaseCommand command);

    CaseRef getCase(Long caseId);

    CaseRef updateCaseHeader(Long caseId, UpdateCaseHeaderCommand command);
}
