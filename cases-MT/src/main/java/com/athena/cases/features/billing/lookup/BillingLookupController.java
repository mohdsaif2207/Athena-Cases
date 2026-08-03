package com.athena.cases.features.billing.lookup;

import com.athena.cases.common.constants.HttpHeaderNames;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.features.billing.dto.BillingAssigneeLookupResponse;
import com.athena.cases.features.billing.dto.HoldLevelsLookupResponse;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.lookup.LookupItem;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Billing-owned lookup REST API under {@code /api/v1/billing/lookups}.
 *
 * <p>Isolated from shared {@code /api/v1/lookups} to minimize merge conflicts with
 * DBM / ExRT / shared lookup work.
 */
@RestController
@RequestMapping("/api/v1/billing/lookups")
public class BillingLookupController {

    private final BillingLookupService billingLookupService;

    public BillingLookupController(BillingLookupService billingLookupService) {
        this.billingLookupService = billingLookupService;
    }

    @GetMapping("/campaigns")
    @PreAuthorize("hasAuthority('PERM_" + PermissionCodes.CASES_CREATE + "') or hasAuthority('PERM_"
            + PermissionCodes.CASES_ACCESS + "')")
    public ResponseEntity<ApiResponse<List<LookupItem>>> campaigns(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ApiResponse.of(billingLookupService.listCampaigns(), resolveRequestId(httpRequest)));
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('PERM_" + PermissionCodes.CASES_CREATE + "') or hasAuthority('PERM_"
            + PermissionCodes.CASES_ACCESS + "')")
    public ResponseEntity<ApiResponse<List<LookupItem>>> products(
            @RequestParam(required = false) String query,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                ApiResponse.of(billingLookupService.listProducts(query), resolveRequestId(httpRequest)));
    }

    @GetMapping("/segments")
    @PreAuthorize("hasAuthority('PERM_" + PermissionCodes.CASES_CREATE + "') or hasAuthority('PERM_"
            + PermissionCodes.CASES_ACCESS + "')")
    public ResponseEntity<ApiResponse<List<LookupItem>>> segments(
            @RequestParam(required = false) String clientId,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                ApiResponse.of(billingLookupService.listSegments(clientId), resolveRequestId(httpRequest)));
    }

    @GetMapping("/parent-cases")
    @PreAuthorize("hasAuthority('PERM_" + PermissionCodes.CASES_CREATE + "') or hasAuthority('PERM_"
            + PermissionCodes.CASES_VIEW + "') or hasAuthority('PERM_" + PermissionCodes.CASES_ACCESS + "')")
    public ResponseEntity<ApiResponse<List<LookupItem>>> parentCases(
            @RequestParam(required = false) String query,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                ApiResponse.of(billingLookupService.findParentCases(query), resolveRequestId(httpRequest)));
    }

    @GetMapping("/hold-levels")
    @PreAuthorize("hasAuthority('PERM_" + PermissionCodes.CASES_VIEW + "')")
    public ResponseEntity<ApiResponse<HoldLevelsLookupResponse>> holdLevels(
            @RequestParam(required = false) BillingHoldType holdType,
            HttpServletRequest httpRequest
    ) {
        HoldLevelsLookupResponse body = new HoldLevelsLookupResponse(
                billingLookupService.listHoldLevels(holdType));
        return ResponseEntity.ok(ApiResponse.of(body, resolveRequestId(httpRequest)));
    }

    @GetMapping("/assignees")
    @PreAuthorize("hasAuthority('PERM_" + PermissionCodes.CASES_VIEW + "')")
    public ResponseEntity<ApiResponse<List<BillingAssigneeLookupResponse>>> assignees(
            HttpServletRequest httpRequest
    ) {
        List<BillingAssigneeLookupResponse> body = billingLookupService.listAssignees().stream()
                .map(BillingAssigneeLookupResponse::from)
                .toList();
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
