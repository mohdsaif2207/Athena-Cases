package com.athena.cases.casemanagement;

import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.common.web.RequestIdFilter;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseManagementService caseManagementService;

    public CaseController(CaseManagementService caseManagementService) {
        this.caseManagementService = caseManagementService;
    }

    /**
     * Lists cases authorized for the JWT principal (case-type RBAC).
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_CASES_VIEW') or hasAuthority('PERM_CASES_ACCESS')")
    public ResponseEntity<ApiResponse<List<CaseListItem>>> list() {
        List<CaseListItem> rows = caseManagementService.listAuthorizedCases();
        return ResponseEntity.ok(ApiResponse.of(rows, currentRequestId()));
    }

    private static String currentRequestId() {
        String id = MDC.get(RequestIdFilter.MDC_KEY);
        return id == null ? "unknown" : id;
    }
}
