package com.athena.cases.features.exrt.controller;

import com.athena.cases.common.dto.ApiResponse;
import com.athena.cases.features.exrt.constant.ExrtConstants;
import com.athena.cases.features.exrt.dto.ExrtLookupItemResponse;
import com.athena.cases.features.exrt.service.ExrtCaseService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ExrtConstants.LOOKUP_BASE)
public class ExrtLookupController {

    private final ExrtCaseService exrtCaseService;

    public ExrtLookupController(ExrtCaseService exrtCaseService) {
        this.exrtCaseService = exrtCaseService;
    }

    @GetMapping("/enums/{lookupType}")
    public ResponseEntity<ApiResponse<List<ExrtLookupItemResponse>>> staticLookups(
            @org.springframework.web.bind.annotation.PathVariable String lookupType,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.of(exrtCaseService.listStaticLookups(lookupType), requestId(request)));
    }

    @GetMapping("/carriers")
    public ResponseEntity<ApiResponse<List<ExrtLookupItemResponse>>> carriers(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.of(exrtCaseService.listCarriers(), requestId(request)));
    }

    @GetMapping("/tier-ii-agents")
    public ResponseEntity<ApiResponse<List<ExrtLookupItemResponse>>> tierIiAgents(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.of(exrtCaseService.listTierIiAgents(), requestId(request)));
    }

    @GetMapping("/assignees")
    public ResponseEntity<ApiResponse<List<ExrtLookupItemResponse>>> assignees(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.of(exrtCaseService.listAssignees(), requestId(request)));
    }

    @GetMapping("/contacts")
    public ResponseEntity<ApiResponse<List<ExrtLookupItemResponse>>> contacts(
            @RequestParam(required = false) String clientId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.of(exrtCaseService.listContacts(clientId), requestId(request)));
    }

    @GetMapping("/reason-codes")
    public ResponseEntity<ApiResponse<List<ExrtLookupItemResponse>>> reasonCodes(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.of(exrtCaseService.listReasonCodes(), requestId(request)));
    }

    private static String requestId(HttpServletRequest request) {
        String header = request.getHeader("X-Request-Id");
        return header == null || header.isBlank() ? UUID.randomUUID().toString() : header;
    }
}
