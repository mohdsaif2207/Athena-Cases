package com.athena.cases.casemanagement;

import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.common.web.RequestIdFilter;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * Updates case header fields (receiving-team Cases Edit). Syncs related workflow/notification rows.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_CASES_EDIT') or hasAuthority('PERM_CASES_ACCESS')")
    public ResponseEntity<ApiResponse<CaseRef>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCaseHeaderCommand command) {
        CaseRef updated = caseManagementService.updateCaseHeader(id, command);
        return ResponseEntity.ok(ApiResponse.of(updated, currentRequestId()));
    }

    private static String currentRequestId() {
        String id = MDC.get(RequestIdFilter.MDC_KEY);
        return id == null ? "unknown" : id;
    }
}
