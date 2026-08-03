package com.athena.cases.lookup;

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
@RequestMapping("/api/v1/lookups")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    /**
     * Returns active case types the caller is authorized to create (RBAC-filtered).
     */
    @GetMapping("/case-types")
    @PreAuthorize("hasAuthority('PERM_CASES_CREATE') or hasAuthority('PERM_CASES_ACCESS')")
    public ResponseEntity<ApiResponse<List<LookupItem>>> caseTypes() {
        return ResponseEntity.ok(ApiResponse.of(lookupService.listAuthorizedCaseTypes(), currentRequestId()));
    }

    @GetMapping("/clients")
    @PreAuthorize("hasAuthority('PERM_CASES_CREATE') or hasAuthority('PERM_CASES_ACCESS')")
    public ResponseEntity<ApiResponse<List<LookupItem>>> clients() {
        return ResponseEntity.ok(ApiResponse.of(lookupService.listActiveClients(), currentRequestId()));
    }

    @GetMapping("/event-ids")
    @PreAuthorize("hasAuthority('PERM_CASES_CREATE') or hasAuthority('PERM_CASES_ACCESS')")
    public ResponseEntity<ApiResponse<List<EventIdLookupItem>>> eventIds() {
        return ResponseEntity.ok(ApiResponse.of(lookupService.listActiveEventIds(), currentRequestId()));
    }

    @GetMapping("/spoken-keys")
    @PreAuthorize("hasAuthority('PERM_CASES_CREATE') or hasAuthority('PERM_CASES_ACCESS')")
    public ResponseEntity<ApiResponse<List<LookupItem>>> spokenKeys() {
        return ResponseEntity.ok(ApiResponse.of(lookupService.listActiveSpokenKeys(), currentRequestId()));
    }

    private static String currentRequestId() {
        String id = MDC.get(RequestIdFilter.MDC_KEY);
        return id == null ? "unknown" : id;
    }
}
