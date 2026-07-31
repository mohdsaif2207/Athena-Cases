package com.athena.cases.casemanagement;

import java.util.List;

/**
 * Port for shared case header lifecycle.
 * Feature modules depend on this interface only — never on peer features.
 */
public interface CaseManagementService {

    CaseRef createCase(CreateCaseCommand command);

    CaseRef getCase(Long caseId);

    CaseRef updateCaseHeader(Long caseId, UpdateCaseHeaderCommand command);

    /**
     * Returns case rows the authenticated user may view (case-type RBAC).
     */
    List<CaseListItem> listAuthorizedCases();
}