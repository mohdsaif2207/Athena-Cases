package com.athena.cases.features.billing.controller;

import com.athena.cases.common.constants.HttpHeaderNames;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.features.billing.dto.BillingAssigneeLookupResponse;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestCreateRequest;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestResponse;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestUpdateRequest;
import com.athena.cases.features.billing.dto.HoldLevelsLookupResponse;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.service.BillingDepartmentRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Billing Department Request REST API — LLD §16 / §17.2.
 *
 * <p>Thin controller: HTTP mapping, auth annotations, {@code @Valid}; orchestration in service.
 * Static {@code /lookups/**} routes are declared before {@code /{caseId}}.
 */
@RestController
@RequestMapping("/api/v1/billing-department-requests")
public class BillingDepartmentRequestController {

    private final BillingDepartmentRequestService billingDepartmentRequestService;

    public BillingDepartmentRequestController(BillingDepartmentRequestService billingDepartmentRequestService) {
        this.billingDepartmentRequestService = billingDepartmentRequestService;
    }

    /**
     * Create Billing Department Request — LLD §16.2.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.CASES_CREATE + "')")
    public ResponseEntity<ApiResponse<BillingDepartmentRequestResponse>> create(
            @Valid @RequestBody BillingDepartmentRequestCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        BillingDepartmentRequestResponse body = billingDepartmentRequestService.create(request);
        URI location = URI.create("/api/v1/billing-department-requests/" + body.caseId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(ApiResponse.of(body, resolveRequestId(httpRequest)));
    }

    /**
     * Hold-level Available options — LLD §16.5.
     */
    @GetMapping("/lookups/hold-levels")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CASES_VIEW + "')")
    public ResponseEntity<ApiResponse<HoldLevelsLookupResponse>> listHoldLevels(
            @RequestParam(required = false) BillingHoldType holdType,
            HttpServletRequest httpRequest
    ) {
        HoldLevelsLookupResponse body = new HoldLevelsLookupResponse(
                billingDepartmentRequestService.listHoldLevels(holdType));
        return ResponseEntity.ok(ApiResponse.of(body, resolveRequestId(httpRequest)));
    }

    /**
     * Users with Billing case access — LLD §16.5.
     */
    @GetMapping("/lookups/assignees")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CASES_VIEW + "')")
    public ResponseEntity<ApiResponse<List<BillingAssigneeLookupResponse>>> listAssignees(
            HttpServletRequest httpRequest
    ) {
        List<BillingAssigneeLookupResponse> body = billingDepartmentRequestService.listAssignees()
                .stream()
                .map(BillingAssigneeLookupResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.of(body, resolveRequestId(httpRequest)));
    }

    /**
     * View / Edit load — LLD §16.3.
     */
    @GetMapping("/{caseId}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CASES_VIEW + "')")
    public ResponseEntity<ApiResponse<BillingDepartmentRequestResponse>> getByCaseId(
            @PathVariable Long caseId,
            HttpServletRequest httpRequest
    ) {
        BillingDepartmentRequestResponse body = billingDepartmentRequestService.getByCaseId(caseId);
        return ResponseEntity.ok(ApiResponse.of(body, resolveRequestId(httpRequest)));
    }

    /**
     * Update editable fields — LLD §16.4.
     */
    @PutMapping("/{caseId}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CASES_EDIT + "')")
    public ResponseEntity<ApiResponse<BillingDepartmentRequestResponse>> update(
            @PathVariable Long caseId,
            @Valid @RequestBody BillingDepartmentRequestUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        BillingDepartmentRequestResponse body = billingDepartmentRequestService.update(caseId, request);
        return ResponseEntity.ok(ApiResponse.of(body, resolveRequestId(httpRequest)));
    }

    private static String resolveRequestId(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaderNames.REQUEST_ID);
        if (header == null || header.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return header;
    }
}
